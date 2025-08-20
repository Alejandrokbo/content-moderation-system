package org.ravenpack.dev;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Map;
import java.util.regex.Pattern;
import org.ravenpack.utils.SpanishTranslationDictionary;

@Path("/dev")
@ApplicationScoped
public class DevStubResource {
    @GET
    @Path("/translate")
    @Produces(MediaType.TEXT_PLAIN)
    public String translate(@QueryParam("q") String q) throws InterruptedException {
        Thread.sleep(ThreadLocalRandom.current().nextLong(50, 201));
        
        if (q == null || q.trim().isEmpty()) {
            return q;
        }
        
        return translateSpanishToEnglish(q.trim());
    }
    
    /**
     * Basic Spanish to English translation using word/phrase substitution
     * @param text The Spanish text to translate
     * @return Translated English text
     */
    private String translateSpanishToEnglish(String text) {
        String result = text.toLowerCase();
        Map<String, String> dictionary = SpanishTranslationDictionary.getSpanishToEnglishDictionary();
        
        // First, try to match complete phrases (longer matches first)
        for (Map.Entry<String, String> entry : dictionary.entrySet()) {
            String spanishPhrase = entry.getKey();
            String englishPhrase = entry.getValue();
            
            // Use word boundaries to ensure we're matching complete words/phrases
            String pattern = "\\b" + Pattern.quote(spanishPhrase) + "\\b";
            result = result.replaceAll(pattern, englishPhrase);
        }
        
        // Preserve original case if the original was all uppercase or title case
        if (text.equals(text.toUpperCase())) {
            result = result.toUpperCase();
        } else if (Character.isUpperCase(text.charAt(0))) {
            result = Character.toUpperCase(result.charAt(0)) + result.substring(1);
        }
        
        return result;
    }

    @GET
    @Path("/score")
    @Produces(MediaType.TEXT_PLAIN)
    public String score(@QueryParam("q") String q) throws InterruptedException {
        Thread.sleep(ThreadLocalRandom.current().nextLong(50, 201));
        double v = Math.abs(q.hashCode() % 1000) / 1000.0;
        return String.valueOf(v);
    }
}

