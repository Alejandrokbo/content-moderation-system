package org.ravenpack.util;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import io.micrometer.core.instrument.MeterRegistry;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.ravenpack.model.InputData;
import org.ravenpack.model.OutputData;
import org.ravenpack.service.AggregationService;
import org.ravenpack.service.ScoringService;
import org.ravenpack.service.TranslationService;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class CsvProcessor {
    
    private static final Logger LOG = Logger.getLogger(CsvProcessor.class);
    
    @Inject TranslationService translation;
    @Inject ScoringService scoring;

    @ConfigProperty(name = "processing.concurrency", defaultValue = "32")
    int concurrency;

    @Inject
    MeterRegistry registry;


    public void process(String inPath, String outPath) throws Exception {
        Instant startTime = Instant.now();
        LOG.infof("🚀 Starting content moderation pipeline...");
        LOG.infof("📂 Input file: %s", inPath);
        LOG.infof("📝 Output file: %s", outPath);
        LOG.infof("⚡ Concurrency level: %d", concurrency);
        var processed = registry.counter("pipeline.messages.processed");
        var failed = registry.counter("pipeline.messages.failed");

        LOG.info("📊 Parsing CSV input file...");
        var settings = new CsvParserSettings();
        settings.setHeaderExtractionEnabled(true);
        settings.setLineSeparatorDetectionEnabled(true);
        settings.setSkipEmptyLines(true);
        settings.trimValues(true);
        var parser = new CsvParser(settings);
        List<com.univocity.parsers.common.record.Record> records =
                parser.parseAllRecords(new FileReader(inPath, StandardCharsets.UTF_8));

        LOG.infof("✅ Parsed %d messages to process", records.size());
        if (records.isEmpty()) {
            LOG.warn("⚠️ No messages found in input file!");
        }

        var agg = new AggregationService();
        var permits = new Semaphore(Math.max(1, concurrency));
        
        // Progress tracking
        final AtomicInteger processedCount = new AtomicInteger(0);
        final AtomicInteger failedCount = new AtomicInteger(0);
        final int totalMessages = records.size();
        
        LOG.infof("🔄 Starting async processing of %d messages...", totalMessages);

        Multi.createFrom().iterable(records)
                .onItem().transform(rec -> {
                    String message = rec.getString("message");
                    String userId = rec.getString("user_id");
                    String normalized = MessageNormalizer.normalize(message);
                    LOG.debugf("💬 Processing message from user %s: '%s' -> '%s'", userId, message, normalized);
                    return new InputData(userId, message);
                })
                .onItem().transformToUniAndMerge(row -> {
                    permits.acquireUninterruptibly();
                    LOG.debugf("🌍 Translating message for user %s...", row.userId());
                    return translation.toEnglish(row.message())
                            .invoke(txt -> LOG.debugf("✅ Translation completed for user %s: '%s'", row.userId(), txt))
                            .flatMap(txt -> {
                                LOG.debugf("🎯 Scoring translated message for user %s...", row.userId());
                                return scoring.score(txt)
                                        .invoke(score -> {
                                            if (score != null) {
                                                LOG.debugf("✅ Score calculated for user %s: %.6f", row.userId(), score);
                                                agg.add(row.userId(), score);
                                                processed.increment();
                                                int currentProcessed = processedCount.incrementAndGet();
                                                if (currentProcessed % 10 == 0 || currentProcessed == totalMessages) {
                                                    LOG.infof("📈 Progress: %d/%d messages processed (%.1f%%)", 
                                                            currentProcessed, totalMessages, 
                                                            (currentProcessed * 100.0) / totalMessages);
                                                }
                                            } else {
                                                LOG.warnf("⚠️ Null score returned for user %s", row.userId());
                                            }
                                        });
                            })
                            .onFailure().invoke(throwable -> {
                                failed.increment();
                                int currentFailed = failedCount.incrementAndGet();
                                LOG.errorf("❌ Processing failed for user %s (failure #%d): %s", 
                                        row.userId(), currentFailed, throwable.getMessage());
                            })
                            .onFailure().recoverWithItem(() -> null)
                            .eventually(() -> permits.release());
                })
                .collect().asList().await().indefinitely();

        Instant processingEndTime = Instant.now();
        Duration processingDuration = Duration.between(startTime, processingEndTime);
        LOG.infof("✅ Async processing completed in %d ms", processingDuration.toMillis());
        
        LOG.info("📊 Generating aggregation results...");
        var snapshot = agg.snapshot();
        int uniqueUsers = ((List<OutputData>) snapshot).size();
        
        LOG.infof("📄 Writing results for %d unique users to output file...", uniqueUsers);
        try (var w = new BufferedWriter(new FileWriter(outPath, StandardCharsets.UTF_8))) {
            w.write("user_id,total_messages,avg_score");
            w.newLine();
            for (OutputData o : snapshot) {
                LOG.debugf("📝 Writing result: %s -> %d messages, avg score: %.6f", 
                        o.userId(), o.totalMessages(), o.avgScore());
                w.write(o.userId() + "," + o.totalMessages() + "," +
                        String.format(java.util.Locale.US, "%.6f", o.avgScore()));
                w.newLine();
            }
        }
        
        Instant endTime = Instant.now();
        Duration totalDuration = Duration.between(startTime, endTime);
        
        LOG.infof("🎉 Content moderation pipeline completed successfully!");
        LOG.infof("📈 Summary: %d messages processed, %d unique users, %d failures", 
                processedCount.get(), uniqueUsers, failedCount.get());
        LOG.infof("⏱️ Total execution time: %d ms", totalDuration.toMillis());
        LOG.infof("⚡ Average processing speed: %.2f messages/second", 
                totalMessages / Math.max(1.0, totalDuration.toMillis() / 1000.0));
        LOG.infof("📁 Results saved to: %s", outPath);
    }
}
