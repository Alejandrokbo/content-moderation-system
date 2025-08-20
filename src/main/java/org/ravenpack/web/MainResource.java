package org.ravenpack.web;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/")
@ApplicationScoped
public class MainResource {

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response index() {
        return Response.ok("<!DOCTYPE html>" +
                "<html><head><title>Content Moderation System</title></head>" +
                "<body><h1>🛡️ Content Moderation System</h1>" +
                "<p>System running correctly.</p>" +
                "<ul>" +
                "<li><a href='/q/health'>Health Check</a></li>" +
                "<li><a href='/q/metrics'>Prometheus Metrics</a></li>" +
                "<li><a href='/api/metrics/simple'>Simple Metrics</a></li>" +
                "<li><a href='/dev/translate?q=test'>Translation Service</a></li>" +
                "<li><a href='/dev/score?q=test'>Scoring Service</a></li>" +
                "</ul>" +
                "</body></html>")
                .build();
    }

    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response status() {
        return Response.ok()
                .entity("{\"status\": \"UP\", \"application\": \"content-moderation-system\", \"version\": \"1.0.0-SNAPSHOT\"}")
                .build();
    }
}
