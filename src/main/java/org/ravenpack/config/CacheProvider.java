package org.ravenpack.config;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class CacheProvider {
    @Inject
    @ConfigProperty(name = "cache.maxSize", defaultValue = "100000")
    int maxSize;

    @Inject
    @ConfigProperty(name = "cache.expireAfterWrite", defaultValue = "300")
    int expireAfterWriteSeconds;

    public AsyncCache<String, Uni<String>> translationCache() {
        return Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireAfterWriteSeconds, TimeUnit.SECONDS)
                .<String, Uni<String>>buildAsync();
    }

    public AsyncCache<String, Uni<Double>> scoringCache() {
        return Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireAfterWriteSeconds, TimeUnit.SECONDS)
                .<String, Uni<Double>>buildAsync();
    }
}
