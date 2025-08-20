package org.ravenpack;

import org.junit.jupiter.api.Test;
import org.ravenpack.utils.MessageNormalizer;

import static org.junit.jupiter.api.Assertions.*;

public class MessageNormalizerTest {

    @Test
    void normalize_basic() {
        assertEquals("hola mundo!", MessageNormalizer.normalize(" hola mundo!  "));
    }

    @Test
    void hash_is_stable() {
        String a = MessageNormalizer.hash("x");
        String b = MessageNormalizer.hash("x");
        assertEquals(a, b);
    }
}
