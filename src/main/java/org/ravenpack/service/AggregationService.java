package org.ravenpack.service;

import org.jboss.logging.Logger;
import org.ravenpack.model.OutputData;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AggregationService {
    
    private static final Logger LOG = Logger.getLogger(AggregationService.class);
    
    public static class Agg {
        public int count;
        public double sum;
    }

    private final Map<String, Agg> map = new ConcurrentHashMap<>();

    public void add(String userId, double score) {
        map.compute(userId, (k, v) -> {
            if (v == null) {
                v = new Agg();
                LOG.debugf("🆕 New user detected: %s", userId);
            }
            v.count += 1;
            v.sum += score;
            double newAvg = v.sum / v.count;
            LOG.debugf("📈 Updated stats for user %s: %d messages, avg score: %.6f (latest: %.6f)", 
                    userId, v.count, newAvg, score);
            return v;
        });
    }

    public Iterable<OutputData> snapshot() {
        var results = map.entrySet().stream()
                .map(e -> new OutputData(e.getKey(), e.getValue().count, e.getValue().sum / e.getValue().count))
                .toList();
        LOG.debugf("📈 Generated aggregation snapshot with %d unique users", results.size());
        return results;
    }
}
