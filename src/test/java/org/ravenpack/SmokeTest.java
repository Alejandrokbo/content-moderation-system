package org.ravenpack;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.ravenpack.utils.CsvProcessor;

import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class SmokeTest {
    @Inject
    CsvProcessor processor;

    @Test
    void runPipeline() throws Exception {
        Path in = Files.createTempFile("in", ".csv");
        Path out = Files.createTempFile("out", ".csv");
        Files.writeString(in, "user_id,message\nu1,hello\nu1,world\nu2,hola\n");
        processor.process(in.toString(), out.toString());
        String content = Files.readString(out);
        assertTrue(content.contains("user_id,total_messages,avg_score"));
    }
}
