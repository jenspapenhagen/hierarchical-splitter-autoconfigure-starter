package de.papenhagen.hierarchical_splitter.core;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;

import java.util.Optional;

public final class OpenAITokenCounter implements TokenCounter {

    private static final EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();
    private static final Optional<Encoding> encoding = registry.getEncoding("cl100k_base");

    @Override
    public int count(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        return encoding.map(e -> e.countTokens(text)).orElse(text.trim().split("\\s+").length);
    }

    public static TokenCounter withEncoding(String encodingName) {
        return text -> {
            if (text == null || text.isBlank()) {
                return 0;
            }
            Optional<Encoding> enc = registry.getEncoding(encodingName);
            return enc.map(e -> e.countTokens(text)).orElse(text.trim().split("\\s+").length);
        };
    }
}
