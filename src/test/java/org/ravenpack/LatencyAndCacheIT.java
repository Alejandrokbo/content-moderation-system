package org.ravenpack;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.*;
import org.ravenpack.util.CsvProcessor;

import java.nio.file.Files;
import java.nio.file.Path;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class LatencyAndCacheIT {

    static WireMockServer wm;

    @BeforeAll
    static void start() {
        wm = new WireMockServer(wireMockConfig().dynamicPort());
        wm.start();

        System.setProperty("quarkus.rest-client.translation.url",
                "http://localhost:" + wm.port());
        System.setProperty("quarkus.rest-client.scoring.url",
                "http://localhost:" + wm.port());

        wm.stubFor(get(urlPathEqualTo("/translate"))
                .willReturn(aResponse()
                        .withFixedDelay(120)
                        .withStatus(200)
                        .withHeader("Content-Type","text/plain")
                        .withBody("translated")));

        wm.stubFor(get(urlPathEqualTo("/score"))
                .willReturn(aResponse()
                        .withFixedDelay(180)
                        .withStatus(200)
                        .withHeader("Content-Type","text/plain")
                        .withBody("0.90")));
    }

    @AfterAll
    static void stop() {
        if (wm != null) wm.stop();
    }

    @Inject
    CsvProcessor processor;

    @Test
    void pipeline_handles_latency_and_caches_duplicates() throws Exception {
        Path in = Files.createTempFile("in", ".csv");
        Path out = Files.createTempFile("out", ".csv");
        Files.writeString(in,
                "user_id,message\n" +
                        "u1,Hola!!!\n" +
                        "u1,  hóla   !!! \n" +
                        "u2,hello\n");

        processor.process(in.toString(), out.toString());

        String content = Files.readString(out);
        assertTrue(content.contains("user_id,total_messages,avg_score"));

        long trCalls = wm.getAllServeEvents().stream()
                .filter(e -> e.getRequest().getUrl().startsWith("/translate"))
                .count();
        long scCalls = wm.getAllServeEvents().stream()
                .filter(e -> e.getRequest().getUrl().startsWith("/score"))
                .count();

        assertTrue(trCalls <= 2);
        assertTrue(scCalls <= 2);
    }
}
