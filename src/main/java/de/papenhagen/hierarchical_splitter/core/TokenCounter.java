package de.papenhagen.hierarchical_splitter.core;

@FunctionalInterface
public interface TokenCounter {

    int count(String text);

    static TokenCounter wordCount() {
        return text -> {
            if (text == null || text.isBlank()) {
                return 0;
            }
            return text.trim().split("\\s+").length;
        };
    }
}
