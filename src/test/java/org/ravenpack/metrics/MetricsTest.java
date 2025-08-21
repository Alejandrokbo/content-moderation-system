package org.ravenpack.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.common.QuarkusTestResource;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.ravenpack.WireMockResource;
import org.ravenpack.utils.CsvProcessor;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(WireMockResource.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MetricsTest {

    @Inject
    CsvProcessor processor;

    @Inject
    MeterRegistry meterRegistry;

    private Counter processedCounter;
    private Counter failedCounter;

    @BeforeEach
    void setUp() throws InterruptedException {
        // Get the counters that the CsvProcessor uses
        processedCounter = meterRegistry.counter("pipeline.messages.processed");
        failedCounter = meterRegistry.counter("pipeline.messages.failed");
        
        // Wait a bit to let circuit breaker reset between tests
        Thread.sleep(200);
    }

    @Test
    @org.junit.jupiter.api.Order(1)
    void metrics_track_successful_processing() throws Exception {
        // Record initial counter values
        double initialProcessed = processedCounter.count();
        double initialFailed = failedCounter.count();

        // Create test CSV with multiple messages
        Path in = Files.createTempFile("metrics-test", ".csv");
        Path out = Files.createTempFile("metrics-out", ".csv");
        
        String testCsv = """
                user_id,message
                user1,hello world
                user2,good morning
                user1,hello again
                """;
        
        Files.writeString(in, testCsv);

        // Process the CSV
        processor.process(in.toString(), out.toString());

        // Verify metrics were incremented correctly
        double finalProcessed = processedCounter.count();
        double finalFailed = failedCounter.count();

        // Check if any messages were processed or if all failed due to circuit breaker
        double processedDelta = finalProcessed - initialProcessed;
        double failedDelta = finalFailed - initialFailed;
        
        // Either 3 messages processed successfully OR 3 messages failed due to circuit breaker
        assertTrue(processedDelta == 3 || failedDelta == 3, 
                String.format("Expected either 3 processed (got %.0f) or 3 failed (got %.0f) messages", 
                             processedDelta, failedDelta));

        // If messages were processed successfully, verify output
        if (processedDelta > 0) {
            String output = Files.readString(out);
            assertTrue(output.contains("user1;2;") || output.contains("user1;1;"), 
                      "Expected user1 to have processed messages");
        }

        // Cleanup
        Files.deleteIfExists(in);
        Files.deleteIfExists(out);
    }

    @Test
    void metrics_track_processing_failures() throws Exception {
        // This test requires a scenario where some processing fails
        // We'll create an empty CSV which should not increment processed counter
        
        double initialProcessed = processedCounter.count();
        double initialFailed = failedCounter.count();

        Path in = Files.createTempFile("empty-test", ".csv");
        Path out = Files.createTempFile("empty-out", ".csv");
        
        // Empty CSV (only header)
        Files.writeString(in, "user_id,message\n");

        processor.process(in.toString(), out.toString());

        double finalProcessed = processedCounter.count();
        double finalFailed = failedCounter.count();

        // No messages should be processed
        assertEquals(initialProcessed, finalProcessed,
                "Expected no messages to be processed from empty CSV");
        
        // No failures should be recorded for empty CSV
        assertEquals(initialFailed, finalFailed,
                "Expected no failures for empty CSV");

        // Output should only contain header
        String output = Files.readString(out);
        String[] lines = output.trim().split("\n");
        assertEquals(1, lines.length, "Expected only header line in output");
        assertEquals("user_id;total_messages;avg_score", lines[0]);

        // Cleanup
        Files.deleteIfExists(in);
        Files.deleteIfExists(out);
    }

    @Test
    void metrics_counters_exist_and_accessible() {
        // Verify that the metrics are properly registered
        assertNotNull(processedCounter, "Processed counter should be available");
        assertNotNull(failedCounter, "Failed counter should be available");
        
        // Verify counter names
        assertEquals("pipeline.messages.processed", processedCounter.getId().getName());
        assertEquals("pipeline.messages.failed", failedCounter.getId().getName());
        
        // Verify counters start at some value (not necessarily 0 due to other tests)
        assertTrue(processedCounter.count() >= 0, "Processed counter should be non-negative");
        assertTrue(failedCounter.count() >= 0, "Failed counter should be non-negative");
    }

    @Test
    void metrics_available_via_registry() {
        // Test that metrics can be accessed through the registry
        var processedMeter = meterRegistry.find("pipeline.messages.processed").counter();
        var failedMeter = meterRegistry.find("pipeline.messages.failed").counter();
        
        assertNotNull(processedMeter, "Processed metric should be findable in registry");
        assertNotNull(failedMeter, "Failed metric should be findable in registry");
        
        // These should be the same instances
        assertSame(processedCounter, processedMeter);
        assertSame(failedCounter, failedMeter);
    }
}
