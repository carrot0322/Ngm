package me.coolmint.ngm.features.memojang;

import net.minecraft.util.Identifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import static me.coolmint.ngm.util.traits.Util.mc;

public class Memojang {
    private static final Map<Identifier, Collection<String>> suggestions;
    private static final Map<Identifier, SuggestionCondition> suggestionConditions;

    private static boolean lastSuggestionsWereConditional;

    static {
        suggestions = new HashMap<>();
        suggestionConditions = new HashMap<>();
        lastSuggestionsWereConditional = true;
    }

    public static void registerSuggestions(Identifier suggestionClass, Collection<String> suggestions) {
        registerSuggestions(suggestionClass, suggestions, SuggestionCondition.ALWAYS);
    }

    public static void registerSuggestions(Identifier suggestionClass, Collection<String> suggestions, SuggestionCondition condition) {
        Memojang.suggestions.put(suggestionClass, suggestions);
        Memojang.suggestionConditions.put(suggestionClass, condition);
    }

    public static Collection<String> getSuggestions(String currentWord) {
        ArrayList<String> applicableSuggestions = new ArrayList<>();

        Memojang.lastSuggestionsWereConditional = false;

        Memojang.suggestionConditions.forEach((suggestionClass, condition) -> {
            boolean shouldSuggest = condition.shouldAddSuggestions(currentWord);
            if (!shouldSuggest) return;

            if (!Memojang.lastSuggestionsWereConditional && condition != SuggestionCondition.ALWAYS) {
                Memojang.lastSuggestionsWereConditional = true;
            }

            applicableSuggestions.addAll(Memojang.suggestions.get(suggestionClass));
        });

        return applicableSuggestions;
    }

    public static boolean wereLastSuggestionsConditional() {
        return Memojang.lastSuggestionsWereConditional;
    }

    Identifier id = new Identifier("memojang", "bad_words");
    ArrayList<String> badWords = new ArrayList<>();

    public Memojang() {}

    public void init() {
        try {
            URL url = new URL("https://pastebin.com/raw/8E4zN237");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                badWords.add(line.trim());
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Memojang.registerSuggestions(id, badWords, SuggestionCondition.ALWAYS);
    }
}
