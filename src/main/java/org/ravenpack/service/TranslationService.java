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
import org.ravenpack.api.TranslationClient;
import org.ravenpack.config.CacheProvider;
import org.ravenpack.utils.MessageNormalizer;

@ApplicationScoped
public class TranslationService {

    private static final Logger LOG = Logger.getLogger(TranslationService.class);

    @Inject @RestClient
    TranslationClient client;

    AsyncCache<String, Uni<String>> cache;

    @Inject
    void init(CacheProvider provider) {
        this.cache = provider.translationCache();
    }

    @Timeout(500)
    @Retry(maxRetries = 2, delay = 50)
    @CircuitBreaker(requestVolumeThreshold = 20, failureRatio = 0.5, delay = 5000)
    public Uni<String> toEnglish(String text) {
        String norm = MessageNormalizer.normalize(text);
        String key = MessageNormalizer.hash("t|" + norm);
        
        // Check if already cached
        boolean isCached = cache.getIfPresent(key) != null;
        if (isCached) {
            LOG.debugf("💾 Cache HIT for translation: '%s'", norm);
        } else {
            LOG.debugf("🌐 Cache MISS for translation: '%s' - calling external API", norm);
        }
        
        return Uni.createFrom().completionStage(
                cache.get(key, (k, exec) ->
                        java.util.concurrent.CompletableFuture.completedFuture(
                                client.translate(norm)
                                    .invoke(result -> LOG.debugf("🌍 Translation API response: '%s' -> '%s'", norm, result))
                                    .memoize().indefinitely()
                        )
                )
        ).flatMap(u -> u);
    }

}
