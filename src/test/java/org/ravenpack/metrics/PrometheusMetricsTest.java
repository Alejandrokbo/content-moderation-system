package org.ravenpack.metrics;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.common.QuarkusTestResource;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.ravenpack.WireMockResource;
import org.ravenpack.utils.CsvProcessor;

import java.nio.file.Files;
import java.nio.file.Path;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@QuarkusTestResource(WireMockResource.class)
class PrometheusMetricsTest {

    @Inject
    CsvProcessor processor;

    @Test
    void prometheus_metrics_endpoint_available() {
        given()
            .when()
                .get("/q/metrics")
            .then()
                .statusCode(200)
                .contentType(anyOf(
                        containsString("text/plain"),
                        containsString("openmetrics-text")))
                .body(containsString("# TYPE"))  // Prometheus format indicator
                .body(containsString("# HELP")); // Prometheus format indicator
    }

    @Test
    void prometheus_exposes_pipeline_metrics() throws Exception {
        // First, generate some metrics by processing a CSV
        Path in = Files.createTempFile("prometheus-test", ".csv");
        Path out = Files.createTempFile("prometheus-out", ".csv");
        
        String testCsv = """
                user_id,message
                testuser,hello prometheus
                testuser,testing metrics
                """;
        
        Files.writeString(in, testCsv);
        processor.process(in.toString(), out.toString());

        // Now check that the metrics are exposed in Prometheus format
        String metricsResponse = given()
            .when()
                .get("/q/metrics")
            .then()
                .statusCode(200)
                .extract().asString();

        // Verify our custom metrics are present
        assert metricsResponse.contains("pipeline_messages_processed") || 
               metricsResponse.contains("pipeline.messages.processed") : 
               "Should contain processed messages metric";
               
        assert metricsResponse.contains("pipeline_messages_failed") || 
               metricsResponse.contains("pipeline.messages.failed") : 
               "Should contain failed messages metric";

        // Cleanup
        Files.deleteIfExists(in);
        Files.deleteIfExists(out);
    }

    @Test
    void prometheus_metrics_include_jvm_metrics() {
        // Verify that basic JVM metrics are also exposed
        String metricsResponse = given()
            .when()
                .get("/q/metrics")
            .then()
                .statusCode(200)
                .extract().asString();

        // Should include JVM metrics (since we enabled them in config)
        assert metricsResponse.contains("jvm_memory") : 
               "Should contain JVM memory metrics";
        assert metricsResponse.contains("jvm_gc") : 
               "Should contain JVM garbage collection metrics";
    }

    @Test
    void metrics_content_type_is_prometheus_format() {
        given()
            .when()
                .get("/q/metrics")
            .then()
                .statusCode(200)
                .header("Content-Type", anyOf(
                        containsString("text/plain"),
                        containsString("openmetrics-text")))
                .body(anyOf(startsWith("# HELP"), startsWith("# EOF"), containsString("# HELP")))  // Prometheus/OpenMetrics format
                .body(containsString("# TYPE")); // Contains type definitions
    }

    @Test
    void metrics_endpoint_performance() {
        // Test that metrics endpoint responds quickly
        long startTime = System.currentTimeMillis();
        
        given()
            .when()
                .get("/q/metrics")
            .then()
                .statusCode(200)
                .time(lessThan(1000L)); // Should respond within 1 second
        
        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;
        
        assert responseTime < 1000 : 
               "Metrics endpoint should respond quickly, took " + responseTime + "ms";
    }
}
