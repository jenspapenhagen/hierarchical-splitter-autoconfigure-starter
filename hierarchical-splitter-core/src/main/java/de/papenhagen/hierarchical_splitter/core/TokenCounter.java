package de.papenhagen.hierarchical_splitter.core;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Interface for counting tokens in text.
 */
@FunctionalInterface
public interface TokenCounter {

    /**
     * Counts the number of tokens in the given text.
     *
     * @param text the text to count
     * @return the number of tokens
     */
    int count(@Nullable String text);

    /**
     * Creates a TokenCounter that counts words.
     *
     * @return the word count token counter
     */
    @NonNull
    static TokenCounter wordCount() {
        return text -> {
            if (text == null || text.isBlank()) {
                return 0;
            }
            return text.trim().split("\\s+").length;
        };
    }
}
