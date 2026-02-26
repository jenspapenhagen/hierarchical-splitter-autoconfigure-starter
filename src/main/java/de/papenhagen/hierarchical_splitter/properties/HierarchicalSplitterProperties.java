package de.papenhagen.hierarchical_splitter.properties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.DeprecatedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;

@Validated
@ConfigurationProperties(prefix = "spring.ai.splitter")
public class HierarchicalSplitterProperties {

    @Min(10)
    @Max(10000)
    private int maxTokens = 1000;

    @Min(1)
    @Max(100)
    private int maxCodeLines = 20;

    @Min(1)
    @Max(100)
    private int maxTableRows = 15;

    @Min(1)
    @Max(100)
    private int maxListItems = 15;

    private boolean preserveCodeLanguage = true;

    private boolean processBlockquotes = true;

    private boolean processHorizontalRules = true;

    private boolean enabled = true;

    public HierarchicalSplitterProperties() {
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    @DeprecatedConfigurationProperty(replacement = "max-code-lines")
    @Deprecated
    public int getMaxCodeLines() {
        return maxCodeLines;
    }

    public void setMaxCodeLines(int maxCodeLines) {
        this.maxCodeLines = maxCodeLines;
    }

    @DeprecatedConfigurationProperty(replacement = "max-table-rows")
    @Deprecated
    public int getMaxTableRows() {
        return maxTableRows;
    }

    public void setMaxTableRows(int maxTableRows) {
        this.maxTableRows = maxTableRows;
    }

    @DeprecatedConfigurationProperty(replacement = "max-list-items")
    @Deprecated
    public int getMaxListItems() {
        return maxListItems;
    }

    public void setMaxListItems(int maxListItems) {
        this.maxListItems = maxListItems;
    }

    public boolean isPreserveCodeLanguage() {
        return preserveCodeLanguage;
    }

    public void setPreserveCodeLanguage(boolean preserveCodeLanguage) {
        this.preserveCodeLanguage = preserveCodeLanguage;
    }

    public boolean isProcessBlockquotes() {
        return processBlockquotes;
    }

    public void setProcessBlockquotes(boolean processBlockquotes) {
        this.processBlockquotes = processBlockquotes;
    }

    public boolean isProcessHorizontalRules() {
        return processHorizontalRules;
    }

    public void setProcessHorizontalRules(boolean processHorizontalRules) {
        this.processHorizontalRules = processHorizontalRules;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HierarchicalSplitterProperties that = (HierarchicalSplitterProperties) o;
        return maxTokens == that.maxTokens &&
                maxCodeLines == that.maxCodeLines &&
                maxTableRows == that.maxTableRows &&
                maxListItems == that.maxListItems &&
                preserveCodeLanguage == that.preserveCodeLanguage &&
                processBlockquotes == that.processBlockquotes &&
                processHorizontalRules == that.processHorizontalRules &&
                enabled == that.enabled;
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxTokens, maxCodeLines, maxTableRows, maxListItems,
                preserveCodeLanguage, processBlockquotes, processHorizontalRules, enabled);
    }

    @Override
    public String toString() {
        return "HierarchicalSplitterProperties{" +
                "maxTokens=" + maxTokens +
                ", maxCodeLines=" + maxCodeLines +
                ", maxTableRows=" + maxTableRows +
                ", maxListItems=" + maxListItems +
                ", preserveCodeLanguage=" + preserveCodeLanguage +
                ", processBlockquotes=" + processBlockquotes +
                ", processHorizontalRules=" + processHorizontalRules +
                ", enabled=" + enabled +
                '}';
    }

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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int maxTokens = 1000;
        private int maxCodeLines = 20;
        private int maxTableRows = 15;
        private int maxListItems = 15;
        private boolean preserveCodeLanguage = true;
        private boolean processBlockquotes = true;
        private boolean processHorizontalRules = true;
        private boolean enabled = true;

        public Builder maxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        public Builder maxCodeLines(int maxCodeLines) {
            this.maxCodeLines = maxCodeLines;
            return this;
        }

        public Builder maxTableRows(int maxTableRows) {
            this.maxTableRows = maxTableRows;
            return this;
        }

        public Builder maxListItems(int maxListItems) {
            this.maxListItems = maxListItems;
            return this;
        }

        public Builder preserveCodeLanguage(boolean preserveCodeLanguage) {
            this.preserveCodeLanguage = preserveCodeLanguage;
            return this;
        }

        public Builder processBlockquotes(boolean processBlockquotes) {
            this.processBlockquotes = processBlockquotes;
            return this;
        }

        public Builder processHorizontalRules(boolean processHorizontalRules) {
            this.processHorizontalRules = processHorizontalRules;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public HierarchicalSplitterProperties build() {
            HierarchicalSplitterProperties props = new HierarchicalSplitterProperties();
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
