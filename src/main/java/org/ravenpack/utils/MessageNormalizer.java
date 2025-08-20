package org.ravenpack.utils;

import java.security.MessageDigest;
import java.text.Normalizer;
import java.util.HexFormat;

public final class MessageNormalizer {
    public static String normalize(String s) {
        if (s == null) return "";
        // First normalize to NFD (decomposed form) to separate diacritics
        String n = Normalizer.normalize(s, Normalizer.Form.NFD);
        // Remove diacritical marks (combining characters)
        n = n.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        // Normalize spaces and convert to lowercase
        n = n.trim().replaceAll("\\s+", " ").toLowerCase();
        // Remove spaces before punctuation marks
        n = n.replaceAll("\\s+([!?.,;:])", "$1");
        return n;
    }
    public static String hash(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(d);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
