package org.ravenpack;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class WireMockResource implements QuarkusTestResourceLifecycleManager {

    static WireMockServer server;

    @Override
    public Map<String, String> start() {
        server = new WireMockServer(0);
        server.start();

        server.stubFor(get(urlPathEqualTo("/translate"))
                .willReturn(aResponse()
                        .withFixedDelay(120)
                        .withStatus(200)
                        .withHeader("Content-Type","text/plain")
                        .withTransformers("response-template")
                        .withBody("{{request.query.q}}")));

        server.stubFor(get(urlPathEqualTo("/score"))
                .willReturn(aResponse()
                        .withFixedDelay(180)
                        .withStatus(200)
                        .withHeader("Content-Type","text/plain")
                        .withBody("0.9")));

        Map<String,String> cfg = new HashMap<>();
        cfg.put("quarkus.rest-client.translation.url", "http://localhost:" + server.port());
        cfg.put("quarkus.rest-client.scoring.url", "http://localhost:" + server.port());
        return cfg;
    }

    @Override
    public void stop() {
        if (server != null) server.stop();
    }
}
