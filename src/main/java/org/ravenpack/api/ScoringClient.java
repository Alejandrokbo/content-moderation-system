package org.ravenpack.api;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import io.smallrye.mutiny.Uni;

@Path("/score")
@RegisterRestClient(configKey = "scoring")
public interface ScoringClient {
    @GET
    Uni<Double> score(@QueryParam("q") String text);
}