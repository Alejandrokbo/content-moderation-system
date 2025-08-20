package org.ravenpack.service;

import com.github.benmanes.caffeine.cache.AsyncCache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import org.ravenpack.api.ScoringClient;
import org.ravenpack.config.CacheProvider;
import org.ravenpack.utils.MessageNormalizer;

@ApplicationScoped
public class ScoringService {

    private static final Logger LOG = Logger.getLogger(ScoringService.class);

    @Inject @RestClient
    ScoringClient client;

    AsyncCache<String, Uni<Double>> cache;

    @Inject
    void init(CacheProvider provider) {
        this.cache = provider.scoringCache();
    }

    @Timeout(500)
    @Retry(maxRetries = 2, delay = 50)
    @CircuitBreaker(delay = 5000)
    public Uni<Double> score(String text) {
        String norm = MessageNormalizer.normalize(text);
        String key = MessageNormalizer.hash("s|" + norm);
        
        // Check if already cached
        boolean isCached = cache.getIfPresent(key) != null;
        if (isCached) {
            LOG.debugf("💾 Cache HIT for scoring: '%s'", norm);
        } else {
            LOG.debugf("🎯 Cache MISS for scoring: '%s' - calling external API", norm);
        }
        
        return Uni.createFrom().completionStage(
                cache.get(key, (k, exec) ->
                        java.util.concurrent.CompletableFuture.completedFuture(
                                client.score(norm)
                                    .invoke(result -> LOG.debugf("🎯 Scoring API response: '%s' -> %.6f", norm, result))
                                    .memoize().indefinitely()
                        )
                )
        ).flatMap(u -> u);
    }
}
