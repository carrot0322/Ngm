package me.coolmint.ngm.features.memojang;

import net.minecraft.util.Identifier;

import java.util.*;

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
    List<String> badWords = List.of("nigger", "느그어매", "병신장애 고아새끼");

    public Memojang() {}

    public void init() {
        Memojang.registerSuggestions(id, badWords, SuggestionCondition.ALWAYS);
    }
}
