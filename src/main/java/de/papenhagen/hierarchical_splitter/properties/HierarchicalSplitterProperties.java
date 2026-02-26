package de.papenhagen.hierarchical_splitter.properties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.DeprecatedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;

/**
 * Configuration properties for HierarchicalTextSplitter.
 */
@Validated
@ConfigurationProperties(prefix = "spring.ai.splitter")
public final class HierarchicalSplitterProperties {

    private static final int MIN_TOKENS = 10;
    private static final int MAX_TOKENS = 10000;
    private static final int MAX_LINES_MIN = 1;
    private static final int MAX_LINES_MAX = 100;
    private static final int DEFAULT_MAX_TOKENS = 1000;
    private static final int DEFAULT_MAX_CODE_LINES = 20;
    private static final int DEFAULT_MAX_TABLE_ROWS = 15;
    private static final int DEFAULT_MAX_LIST_ITEMS = 15;

    @Min(MIN_TOKENS)
    @Max(MAX_TOKENS)
    private int maxTokens = DEFAULT_MAX_TOKENS;

    @Min(MAX_LINES_MIN)
    @Max(MAX_LINES_MAX)
    private int maxCodeLines = DEFAULT_MAX_CODE_LINES;

    @Min(MAX_LINES_MIN)
    @Max(MAX_LINES_MAX)
    private int maxTableRows = DEFAULT_MAX_TABLE_ROWS;

    @Min(MAX_LINES_MIN)
    @Max(MAX_LINES_MAX)
    private int maxListItems = DEFAULT_MAX_LIST_ITEMS;

    private boolean preserveCodeLanguage = true;

    private boolean processBlockquotes = true;

    private boolean processHorizontalRules = true;

    private boolean enabled = true;

    /**
     * Creates a new instance.
     */
    public HierarchicalSplitterProperties() {
    }

    /**
     * Returns the maximum number of tokens per chunk.
     *
     * @return the max tokens
     */
    public int getMaxTokens() {
        return maxTokens;
    }

    /**
     * Sets the maximum number of tokens per chunk.
     *
     * @param maxTokens the max tokens
     */
    public void setMaxTokens(final int maxTokens) {
        this.maxTokens = maxTokens;
    }

    /**
     * Returns the maximum number of lines in a code block.
     *
     * @return the max code lines
     */
    @DeprecatedConfigurationProperty(replacement = "max-code-lines")
    @Deprecated
    public int getMaxCodeLines() {
        return maxCodeLines;
    }

    /**
     * Sets the maximum number of lines in a code block.
     *
     * @param maxCodeLines the max code lines
     */
    public void setMaxCodeLines(final int maxCodeLines) {
        this.maxCodeLines = maxCodeLines;
    }

    /**
     * Returns the maximum number of rows in a table.
     *
     * @return the max table rows
     */
    @DeprecatedConfigurationProperty(replacement = "max-table-rows")
    @Deprecated
    public int getMaxTableRows() {
        return maxTableRows;
    }

    /**
     * Sets the maximum number of rows in a table.
     *
     * @param maxTableRows the max table rows
     */
    public void setMaxTableRows(final int maxTableRows) {
        this.maxTableRows = maxTableRows;
    }

    /**
     * Returns the maximum number of list items.
     *
     * @return the max list items
     */
    @DeprecatedConfigurationProperty(replacement = "max-list-items")
    @Deprecated
    public int getMaxListItems() {
        return maxListItems;
    }

    /**
     * Sets the maximum number of list items.
     *
     * @param maxListItems the max list items
     */
    public void setMaxListItems(final int maxListItems) {
        this.maxListItems = maxListItems;
    }

    /**
     * Returns whether to preserve code language annotations.
     *
     * @return true if preserving
     */
    public boolean isPreserveCodeLanguage() {
        return preserveCodeLanguage;
    }

    /**
     * Sets whether to preserve code language annotations.
     *
     * @param preserveCodeLanguage true to preserve
     */
    public void setPreserveCodeLanguage(final boolean preserveCodeLanguage) {
        this.preserveCodeLanguage = preserveCodeLanguage;
    }

    /**
     * Returns whether to process blockquotes.
     *
     * @return true if processing
     */
    public boolean isProcessBlockquotes() {
        return processBlockquotes;
    }

    /**
     * Sets whether to process blockquotes.
     *
     * @param processBlockquotes true to process
     */
    public void setProcessBlockquotes(final boolean processBlockquotes) {
        this.processBlockquotes = processBlockquotes;
    }

    /**
     * Returns whether to process horizontal rules.
     *
     * @return true if processing
     */
    public boolean isProcessHorizontalRules() {
        return processHorizontalRules;
    }

    /**
     * Sets whether to process horizontal rules.
     *
     * @param processHorizontalRules true to process
     */
    public void setProcessHorizontalRules(final boolean processHorizontalRules) {
        this.processHorizontalRules = processHorizontalRules;
    }

    /**
     * Returns whether the splitter is enabled.
     *
     * @return true if enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether the splitter is enabled.
     *
     * @param enabled true to enable
     */
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final HierarchicalSplitterProperties that =
                (HierarchicalSplitterProperties) o;
        return maxTokens == that.maxTokens
                && maxCodeLines == that.maxCodeLines
                && maxTableRows == that.maxTableRows
                && maxListItems == that.maxListItems
                && preserveCodeLanguage == that.preserveCodeLanguage
                && processBlockquotes == that.processBlockquotes
                && processHorizontalRules == that.processHorizontalRules
                && enabled == that.enabled;
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxTokens, maxCodeLines, maxTableRows, maxListItems,
                preserveCodeLanguage, processBlockquotes,
                processHorizontalRules, enabled);
    }

    @Override
    public String toString() {
        return "HierarchicalSplitterProperties{"
                + "maxTokens=" + maxTokens
                + ", maxCodeLines=" + maxCodeLines
                + ", maxTableRows=" + maxTableRows
                + ", maxListItems=" + maxListItems
                + ", preserveCodeLanguage=" + preserveCodeLanguage
                + ", processBlockquotes=" + processBlockquotes
                + ", processHorizontalRules=" + processHorizontalRules
                + ", enabled=" + enabled
                + '}';
    }

    /**
     * Creates a Builder from this properties instance.
     *
     * @return the builder
     */
    public Builder toBuilder() {
        return new Builder()
                .maxTokens(maxTokens)
                .maxCodeLines(maxCodeLines)
                .maxTableRows(maxTableRows)
                .maxListItems(maxListItems)
                .preserveCodeLanguage(preserveCodeLanguage)
                .processBlockquotes(processBlockquotes)
                .processHorizontalRules(processHorizontalRules)
                .enabled(enabled);
    }

    /**
     * Creates a new Builder.
     *
     * @return the builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for HierarchicalSplitterProperties.
     */
    public static final class Builder {
        private static final int DEFAULT_MAX_TOKENS = 1000;
        private static final int DEFAULT_MAX_CODE_LINES = 20;
        private static final int DEFAULT_MAX_TABLE_ROWS = 15;
        private static final int DEFAULT_MAX_LIST_ITEMS = 15;

        private int maxTokens = DEFAULT_MAX_TOKENS;
        private int maxCodeLines = DEFAULT_MAX_CODE_LINES;
        private int maxTableRows = DEFAULT_MAX_TABLE_ROWS;
        private int maxListItems = DEFAULT_MAX_LIST_ITEMS;
        private boolean preserveCodeLanguage = true;
        private boolean processBlockquotes = true;
        private boolean processHorizontalRules = true;
        private boolean enabled = true;

        /**
         * Creates a new Builder.
         */
        public Builder() {
        }

        /**
         * Sets the maximum number of tokens per chunk.
         *
         * @param maxTokens the max tokens
         * @return this builder
         */
        public Builder maxTokens(final int maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        /**
         * Sets the maximum number of lines in a code block.
         *
         * @param maxCodeLines the max code lines
         * @return this builder
         */
        public Builder maxCodeLines(final int maxCodeLines) {
            this.maxCodeLines = maxCodeLines;
            return this;
        }

        /**
         * Sets the maximum number of rows in a table.
         *
         * @param maxTableRows the max table rows
         * @return this builder
         */
        public Builder maxTableRows(final int maxTableRows) {
            this.maxTableRows = maxTableRows;
            return this;
        }

        /**
         * Sets the maximum number of list items.
         *
         * @param maxListItems the max list items
         * @return this builder
         */
        public Builder maxListItems(final int maxListItems) {
            this.maxListItems = maxListItems;
            return this;
        }

        /**
         * Sets whether to preserve code language annotations.
         *
         * @param preserveCodeLanguage true to preserve
         * @return this builder
         */
        public Builder preserveCodeLanguage(final boolean preserveCodeLanguage) {
            this.preserveCodeLanguage = preserveCodeLanguage;
            return this;
        }

        /**
         * Sets whether to process blockquotes.
         *
         * @param processBlockquotes true to process
         * @return this builder
         */
        public Builder processBlockquotes(final boolean processBlockquotes) {
            this.processBlockquotes = processBlockquotes;
            return this;
        }

        /**
         * Sets whether to process horizontal rules.
         *
         * @param processHorizontalRules true to process
         * @return this builder
         */
        public Builder processHorizontalRules(final boolean processHorizontalRules) {
            this.processHorizontalRules = processHorizontalRules;
            return this;
        }

        /**
         * Sets whether the splitter is enabled.
         *
         * @param enabled true to enable
         * @return this builder
         */
        public Builder enabled(final boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Builds the properties.
         *
         * @return the properties
         */
        public HierarchicalSplitterProperties build() {
            final HierarchicalSplitterProperties props =
                    new HierarchicalSplitterProperties();
            props.maxTokens = this.maxTokens;
            props.maxCodeLines = this.maxCodeLines;
            props.maxTableRows = this.maxTableRows;
            props.maxListItems = this.maxListItems;
            props.preserveCodeLanguage = this.preserveCodeLanguage;
            props.processBlockquotes = this.processBlockquotes;
            props.processHorizontalRules = this.processHorizontalRules;
            props.enabled = this.enabled;
            return props;
        }
    }
}
