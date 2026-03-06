package de.papenhagen.hierarchical_splitter.core;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.ModelType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Token counter supporting multiple encodings via TikToken.
 * <p>
 * Supports the following encodings:
 * <ul>
 *   <li>cl100k_base - OpenAI (GPT-4, GPT-3.5 Turbo)</li>
 *   <li>p50k_base - OpenAI (GPT-3, Codex)</li>
 *   <li>p50k_edit - OpenAI (code-davinci-002)</li>
 *   <li>r50k_base - OpenAI (GPT-3)</li>
 * </ul>
 * </p>
 *
 * @see <a href="https://github.com/openai/tiktoken">TikToken</a>
 */
public final class TikTokenCounter implements TokenCounter {

    private static final EncodingRegistry REGISTRY = Encodings.newDefaultEncodingRegistry();

    private final Encoding encoding;

    /**
     * Creates a TikTokenCounter with the specified encoding name.
     *
     * @param encodingName the encoding name (e.g., "cl100k_base")
     */
    public TikTokenCounter(@NonNull final String encodingName) {
        this.encoding = REGISTRY.getEncoding(encodingName)
                .orElseThrow(() -> new IllegalArgumentException("Unknown encoding: " + encodingName));
    }

    /**
     * Creates a TikTokenCounter for a specific model.
     *
     * @param modelType the model type
     */
    public TikTokenCounter(@NonNull final ModelType modelType) {
        this.encoding = REGISTRY.getEncodingForModel(modelType);
    }

    @Override
    public int count(@Nullable final String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        return encoding.countTokens(text);
    }

    /**
     * Creates a TokenCounter for GPT-4 models.
     *
     * @return the token counter
     */
    @NonNull
    public static TikTokenCounter forGpt4() {
        return new TikTokenCounter("cl100k_base");
    }

    /**
     * Creates a TokenCounter for GPT-3.5 Turbo.
     *
     * @return the token counter
     */
    @NonNull
    public static TikTokenCounter forGpt35Turbo() {
        return new TikTokenCounter("cl100k_base");
    }

    /**
     * Creates a TokenCounter for GPT-3 models.
     *
     * @return the token counter
     */
    @NonNull
    public static TikTokenCounter forGpt3() {
        return new TikTokenCounter("r50k_base");
    }

    /**
     * Creates a TokenCounter for Codex models.
     *
     * @return the token counter
     */
    @NonNull
    public static TikTokenCounter forCodex() {
        return new TikTokenCounter("p50k_base");
    }
}
