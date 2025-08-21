package org.ravenpack.web;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.ravenpack.utils.CsvProcessor;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Path("/api/csv")
@Produces(MediaType.APPLICATION_JSON)
public class CsvProcessingResource {

    private static final Logger LOG = Logger.getLogger(CsvProcessingResource.class);

    @Inject
    CsvProcessor csvProcessor;

    @POST
    @Path("/process")
    @Consumes({MediaType.TEXT_PLAIN, "text/csv"})
    @Produces(MediaType.TEXT_PLAIN)
    public Response processCsvContent(String csvContent) {
        LOG.infof("🚀 Received CSV processing request with %d characters", csvContent.length());

        try {
            java.nio.file.Path currentDir = java.nio.file.Paths.get(System.getProperty("user.dir"));
            java.nio.file.Path projectRoot = currentDir;
            
            while (projectRoot != null && !Files.exists(projectRoot.resolve("build.gradle.kts"))) {
                projectRoot = projectRoot.getParent();
            }
            
            if (projectRoot == null) {
                projectRoot = currentDir;
            }
            
            java.nio.file.Path outputDir = projectRoot.resolve("output");
            if (!Files.exists(outputDir)) {
                Files.createDirectories(outputDir);
            }

            java.nio.file.Path inputFile = Files.createTempFile("input-", ".csv");
            String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            java.nio.file.Path outputFile = outputDir.resolve("processed-" + timestamp + ".csv");

            // Write input content to temp file
            Files.writeString(inputFile, csvContent, StandardCharsets.UTF_8);
            LOG.infof("📝 Written input to: %s", inputFile);

            // Process the CSV
            long startTime = System.currentTimeMillis();
            csvProcessor.process(inputFile.toString(), outputFile.toString());
            long processingTime = System.currentTimeMillis() - startTime;

            // Read the result
            String result = Files.readString(outputFile, StandardCharsets.UTF_8);

            // Cleanup input file only, keep output file in output directory
            Files.deleteIfExists(inputFile);

            LOG.infof("✅ Processing completed in %d ms", processingTime);
            LOG.infof("💾 Output file saved to: %s", outputFile.toAbsolutePath());

            return Response.ok()
                    .entity(result)
                    .header("X-Processing-Time-Ms", processingTime)
                    .header("X-Output-File", outputFile.getFileName().toString())
                    .header("Content-Type", "text/csv")
                    .build();

        } catch (Exception e) {
            LOG.errorf("❌ Error processing CSV: %s", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error processing CSV: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/sample")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getSampleCsv() {
        String sampleCsv = """
                user_id;message
                alice;"Hello everyone! How are you doing today?"
                bob;"This is terrible! I hate everything about this."
                alice;"hëllo everyone!!! How are you doing today? 🌟"
                charlie;"Good morning! Have a wonderful day!"
                bob;"This is TERRIBLE! I HATE everything about this!!!"
                alice;"Hello everyone! How are you doing today?   "
                diana;"The weather is beautiful today."
                bob;"this is terrible i hate everything about this"
                """;

        return Response.ok(sampleCsv)
                .header("Content-Type", "text/csv")
                .header("Content-Disposition", "attachment; filename=\"sample-input.csv\"")
                .build();
    }

    @GET
    @Path("/health")
    public Response health() {
        return Response.ok()
                .entity("{\"status\":\"UP\",\"service\":\"CSV Processing API\"}")
                .build();
    }
}
