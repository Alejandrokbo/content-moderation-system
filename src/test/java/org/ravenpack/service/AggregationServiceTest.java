package org.ravenpack.service;

import org.junit.jupiter.api.Test;
import org.ravenpack.model.OutputData;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class AggregationServiceTest {
    @Test
    void aggregates_ok() {
        AggregationService agg = new AggregationService();
        agg.add("u1", 0.5);
        agg.add("u1", 1.0);
        agg.add("u2", 0.0);
        List<OutputData> list = (List<OutputData>) agg.snapshot();
        double avgU1 = list.stream().filter(r -> r.userId().equals("u1")).findFirst().get().avgScore();
        double avgU2 = list.stream().filter(r -> r.userId().equals("u2")).findFirst().get().avgScore();
        assertEquals(0.75, avgU1, 1e-9);
        assertEquals(0.0, avgU2, 1e-9);
    }
}
