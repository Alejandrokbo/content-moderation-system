package org.ravenpack.it;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.*;
import org.ravenpack.utils.CsvProcessor;
import java.nio.file.Files;
import java.nio.file.Path;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class FailureResilienceIT {
    static WireMockServer wm;
    @BeforeAll
    static void start() {
        wm = new WireMockServer(wireMockConfig().dynamicPort());
        wm.start();
        System.setProperty("quarkus.rest-client.translation.url","http://localhost:"+wm.port()+"/translate");
        System.setProperty("quarkus.rest-client.scoring.url","http://localhost:"+wm.port()+"/score");
        wm.stubFor(get(urlPathEqualTo("/translate"))
                .willReturn(aResponse().withFixedDelay(80).withStatus(200).withBody("translated")));
        wm.stubFor(get(urlPathEqualTo("/score"))
                .inScenario("fail-then-ok")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withStatus(500).withFixedDelay(50))
                .willSetStateTo("ok"));
        wm.stubFor(get(urlPathEqualTo("/score"))
                .inScenario("fail-then-ok")
                .whenScenarioStateIs("ok")
                .willReturn(aResponse().withStatus(200).withFixedDelay(80).withBody("0.5")));
    }
    @AfterAll
    static void stop() { if (wm != null) wm.stop(); }

    @Inject CsvProcessor processor;

    @Test
    void pipeline_survives_retry_timeout() throws Exception {
        Path in = Files.createTempFile("in",".csv");
        Path out = Files.createTempFile("out",".csv");
        Files.writeString(in,"user_id,message\nu1,err\nu2,ok\n");
        processor.process(in.toString(), out.toString());
        String content = Files.readString(out);
        assertTrue(content.contains("user_id,total_messages,avg_score"));
    }
}
