package de.papenhagen.hierarchical_splitter.core;

import java.util.Optional;
import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Token counter using OpenAI's encoding.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public final class OpenAITokenCounter implements TokenCounter {

    private static final EncodingRegistry REGISTRY = Encodings.newDefaultEncodingRegistry();
    private static final Optional<Encoding> ENCODING = REGISTRY.getEncoding("cl100k_base");

    @Override
    public int count(@Nullable final String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        return ENCODING.map(e -> e.countTokens(text))
                .orElse(text.trim().split("\\s+").length);
    }

    /**
     * Creates a TokenCounter with a specific encoding.
     *
     * @param encodingName the encoding name
     * @return the token counter
     */
    @NonNull
    public static TokenCounter withEncoding(@NonNull final String encodingName) {
        return text -> {
            if (text == null || text.isBlank()) {
                return 0;
            }
            final Optional<Encoding> enc = REGISTRY.getEncoding(encodingName);
            return enc.map(e -> e.countTokens(text))
                    .orElse(text.trim().split("\\s+").length);
        };
    }
}
