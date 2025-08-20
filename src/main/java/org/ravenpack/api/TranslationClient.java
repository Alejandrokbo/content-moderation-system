package org.ravenpack.api;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import io.smallrye.mutiny.Uni;

@Path("/translate")
@RegisterRestClient(configKey = "translation")
public interface TranslationClient {
    @GET
    Uni<String> translate(@QueryParam("q") String text);
}
