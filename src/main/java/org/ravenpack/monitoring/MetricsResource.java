package org.ravenpack.monitoring;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/metrics")
@ApplicationScoped
public class MetricsResource {

    @GET
    @Path("/simple")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSimpleMetrics() {
        // Métricas básicas del sistema
        long totalMemory = Runtime.getRuntime().totalMemory();
        long freeMemory = Runtime.getRuntime().freeMemory();
        long usedMemory = totalMemory - freeMemory;
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        
        return Response.ok()
                .entity("{\n" +
                        "  \"system\": {\n" +
                        "    \"total_memory_bytes\": " + totalMemory + ",\n" +
                        "    \"free_memory_bytes\": " + freeMemory + ",\n" +
                        "    \"used_memory_bytes\": " + usedMemory + ",\n" +
                        "    \"available_processors\": " + availableProcessors + "\n" +
                        "  },\n" +
                        "  \"application\": {\n" +
                        "    \"status\": \"UP\",\n" +
                        "    \"name\": \"content-moderation-system\",\n" +
                        "    \"version\": \"1.0.0-SNAPSHOT\"\n" +
                        "  }\n" +
                        "}")
                .build();
    }
}
