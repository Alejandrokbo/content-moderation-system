package org.ravenpack.dev;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.concurrent.ThreadLocalRandom;

@Path("/dev")
@ApplicationScoped
public class DevStubResource {
    @GET
    @Path("/translate")
    @Produces(MediaType.TEXT_PLAIN)
    public String translate(@QueryParam("q") String q) throws InterruptedException {
        Thread.sleep(ThreadLocalRandom.current().nextLong(50, 201));
        return q;
    }

    @GET
    @Path("/score")
    @Produces(MediaType.TEXT_PLAIN)
    public String score(@QueryParam("q") String q) throws InterruptedException {
        Thread.sleep(ThreadLocalRandom.current().nextLong(50, 201));
        double v = Math.abs(q.hashCode() % 1000) / 1000.0;
        return String.valueOf(v);
    }
}

