package org.ravenpack.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Dictionary containing basic Spanish to English translations.
 * This class provides a static map with common Spanish words and phrases
 * and their English equivalents.
 */
public class SpanishTranslationDictionary {
    
    // Basic Spanish to English translation dictionary
    private static final Map<String, String> SPANISH_TO_ENGLISH = new HashMap<>();
    
    static {
        // Common greetings and basic words
        SPANISH_TO_ENGLISH.put("hola", "hello");
        SPANISH_TO_ENGLISH.put("adios", "goodbye");
        SPANISH_TO_ENGLISH.put("gracias", "thanks");
        SPANISH_TO_ENGLISH.put("por favor", "please");
        SPANISH_TO_ENGLISH.put("si", "yes");
        SPANISH_TO_ENGLISH.put("sí", "yes");
        SPANISH_TO_ENGLISH.put("no", "no");
        SPANISH_TO_ENGLISH.put("buenos dias", "good morning");
        SPANISH_TO_ENGLISH.put("buenos días", "good morning");
        SPANISH_TO_ENGLISH.put("buenas tardes", "good afternoon");
        SPANISH_TO_ENGLISH.put("buenas noches", "good night");
        SPANISH_TO_ENGLISH.put("como estas", "how are you");
        SPANISH_TO_ENGLISH.put("cómo estás", "how are you");
        SPANISH_TO_ENGLISH.put("que tal", "how are you");
        SPANISH_TO_ENGLISH.put("qué tal", "how are you");
        
        // Common words
        SPANISH_TO_ENGLISH.put("agua", "water");
        SPANISH_TO_ENGLISH.put("casa", "house");
        SPANISH_TO_ENGLISH.put("perro", "dog");
        SPANISH_TO_ENGLISH.put("gato", "cat");
        SPANISH_TO_ENGLISH.put("coche", "car");
        SPANISH_TO_ENGLISH.put("libro", "book");
        SPANISH_TO_ENGLISH.put("tiempo", "time");
        SPANISH_TO_ENGLISH.put("dinero", "money");
        SPANISH_TO_ENGLISH.put("trabajo", "work");
        SPANISH_TO_ENGLISH.put("escuela", "school");
        SPANISH_TO_ENGLISH.put("familia", "family");
        SPANISH_TO_ENGLISH.put("amigo", "friend");
        SPANISH_TO_ENGLISH.put("amor", "love");
        SPANISH_TO_ENGLISH.put("vida", "life");
        SPANISH_TO_ENGLISH.put("mundo", "world");
        SPANISH_TO_ENGLISH.put("día", "day");
        SPANISH_TO_ENGLISH.put("dia", "day");
        SPANISH_TO_ENGLISH.put("noche", "night");
        SPANISH_TO_ENGLISH.put("mañana", "morning");
        SPANISH_TO_ENGLISH.put("tarde", "afternoon");
        
        // Numbers
        SPANISH_TO_ENGLISH.put("uno", "one");
        SPANISH_TO_ENGLISH.put("dos", "two");
        SPANISH_TO_ENGLISH.put("tres", "three");
        SPANISH_TO_ENGLISH.put("cuatro", "four");
        SPANISH_TO_ENGLISH.put("cinco", "five");
        
        // Emotions and adjectives
        SPANISH_TO_ENGLISH.put("feliz", "happy");
        SPANISH_TO_ENGLISH.put("triste", "sad");
        SPANISH_TO_ENGLISH.put("enojado", "angry");
        SPANISH_TO_ENGLISH.put("bueno", "good");
        SPANISH_TO_ENGLISH.put("malo", "bad");
        SPANISH_TO_ENGLISH.put("grande", "big");
        SPANISH_TO_ENGLISH.put("pequeño", "small");
        SPANISH_TO_ENGLISH.put("nuevo", "new");
        SPANISH_TO_ENGLISH.put("viejo", "old");
        
        // Verbs
        SPANISH_TO_ENGLISH.put("ser", "to be");
        SPANISH_TO_ENGLISH.put("estar", "to be");
        SPANISH_TO_ENGLISH.put("tener", "to have");
        SPANISH_TO_ENGLISH.put("hacer", "to do");
        SPANISH_TO_ENGLISH.put("ir", "to go");
        SPANISH_TO_ENGLISH.put("venir", "to come");
        SPANISH_TO_ENGLISH.put("ver", "to see");
        SPANISH_TO_ENGLISH.put("decir", "to say");
        SPANISH_TO_ENGLISH.put("querer", "to want");
        SPANISH_TO_ENGLISH.put("poder", "to be able");
        SPANISH_TO_ENGLISH.put("saber", "to know");
        
        // Common phrases
        SPANISH_TO_ENGLISH.put("me gusta", "I like");
        SPANISH_TO_ENGLISH.put("no me gusta", "I don't like");
        SPANISH_TO_ENGLISH.put("tengo hambre", "I'm hungry");
        SPANISH_TO_ENGLISH.put("tengo sed", "I'm thirsty");
        SPANISH_TO_ENGLISH.put("estoy bien", "I'm fine");
        SPANISH_TO_ENGLISH.put("no entiendo", "I don't understand");
        SPANISH_TO_ENGLISH.put("habla despacio", "speak slowly");
        SPANISH_TO_ENGLISH.put("cuanto cuesta", "how much does it cost");
        SPANISH_TO_ENGLISH.put("cuánto cuesta", "how much does it cost");
        
        // Articles and connectors
        SPANISH_TO_ENGLISH.put("el", "the");
        SPANISH_TO_ENGLISH.put("la", "the");
        SPANISH_TO_ENGLISH.put("los", "the");
        SPANISH_TO_ENGLISH.put("las", "the");
        SPANISH_TO_ENGLISH.put("un", "a");
        SPANISH_TO_ENGLISH.put("una", "a");
        SPANISH_TO_ENGLISH.put("y", "and");
        SPANISH_TO_ENGLISH.put("o", "or");
        SPANISH_TO_ENGLISH.put("pero", "but");
        SPANISH_TO_ENGLISH.put("con", "with");
        SPANISH_TO_ENGLISH.put("sin", "without");
        SPANISH_TO_ENGLISH.put("para", "for");
        SPANISH_TO_ENGLISH.put("por", "by");
        SPANISH_TO_ENGLISH.put("de", "of");
        SPANISH_TO_ENGLISH.put("en", "in");
        SPANISH_TO_ENGLISH.put("a", "to");
    }
    
    /**
     * Returns the Spanish to English translation dictionary.
     * @return A map containing Spanish words/phrases as keys and their English translations as values
     */
    public static Map<String, String> getSpanishToEnglishDictionary() {
        return new HashMap<>(SPANISH_TO_ENGLISH);
    }
    
    /**
     * Gets the English translation for a Spanish word or phrase.
     * @param spanishText The Spanish text to translate
     * @return The English translation if found, otherwise the original text
     */
    public static String translate(String spanishText) {
        if (spanishText == null) {
            return null;
        }
        return SPANISH_TO_ENGLISH.getOrDefault(spanishText.toLowerCase(), spanishText);
    }
    
    /**
     * Checks if a Spanish word or phrase exists in the dictionary.
     * @param spanishText The Spanish text to check
     * @return true if the text exists in the dictionary, false otherwise
     */
    public static boolean containsSpanishText(String spanishText) {
        if (spanishText == null) {
            return false;
        }
        return SPANISH_TO_ENGLISH.containsKey(spanishText.toLowerCase());
    }
}
