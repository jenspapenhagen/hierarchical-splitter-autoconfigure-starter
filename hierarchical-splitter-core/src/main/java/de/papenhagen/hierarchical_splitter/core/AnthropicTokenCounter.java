package de.papenhagen.hierarchical_splitter.core;

import java.util.Optional;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Token counter optimized for Anthropic Claude models.
 * <p>
 * Anthropic's Claude models use the same tokenization as OpenAI (cl100k_base)
 * for most models. This counter can be configured to use different encodings
 * if needed for specific Claude versions.
 * </p>
 *
 * @see <a href="https://docs.anthropic.com/en/docs/build-with-claude/token-counting">Anthropic Token Counting</a>
 */
public final class AnthropicTokenCounter implements TokenCounter {

    private static final EncodingRegistry REGISTRY = Encodings.newDefaultEncodingRegistry();
    private static final double CHARS_PER_TOKEN = 4.0;

    private final String tokenizerEncoding;

    /**
     * Creates an AnthropicTokenCounter using the default cl100k_base encoding.
     */
    public AnthropicTokenCounter() {
        this("cl100k_base");
    }

    /**
     * Creates an AnthropicTokenCounter with a specific encoding.
     *
     * @param encName the encoding name to use
     */
    public AnthropicTokenCounter(@NonNull final String encName) {
        this.tokenizerEncoding = encName;
    }

    @Override
    public int count(@Nullable final String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        final Optional<Encoding> encoding = REGISTRY.getEncoding(tokenizerEncoding);
        return encoding.map(e -> e.countTokens(text))
                .orElseGet(() -> estimateTokens(text));
    }

    private int estimateTokens(final String text) {
        return (int) Math.ceil(text.length() / CHARS_PER_TOKEN);
    }

    /**
     * Creates a TokenCounter for Claude 3 models.
     *
     * @return the token counter
     */
    @NonNull
    public static AnthropicTokenCounter forClaude3() {
        return new AnthropicTokenCounter("cl100k_base");
    }

    /**
     * Creates a TokenCounter for Claude 2 models.
     *
     * @return the token counter
     */
    @NonNull
    public static AnthropicTokenCounter forClaude2() {
        return new AnthropicTokenCounter("cl100k_base");
    }
}
