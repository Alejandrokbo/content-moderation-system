// src/test/java/org/ravenpack/util/CsvProcessorOneRowTest.java
package org.ravenpack.util;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.common.QuarkusTestResource;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.ravenpack.WireMockResource;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@QuarkusTestResource(WireMockResource.class)
class CsvProcessorOneRowTest {

    @Inject
    CsvProcessor processor;

    @Test
    void one_row_generates_output() throws Exception {
        Path in = Files.createTempFile("in", ".csv");
        Path out = Files.createTempFile("out", ".csv");
        Files.writeString(in, "user_id,message\nu1,hello\n");

        processor.process(in.toString(), out.toString());

        String outTxt = Files.readString(out);

        assertTrue(outTxt.startsWith("user_id,total_messages,avg_score"),
                "Expected header, got:\n" + outTxt);
        assertTrue(outTxt.contains("u1,1,"),
                "Expected row for u1 with total_messages=1, got:\n" + outTxt);
    }
}
