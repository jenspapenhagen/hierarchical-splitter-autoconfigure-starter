package de.papenhagen.hierarchical_splitter.autoconfigure;

import de.papenhagen.hierarchical_splitter.core.HierarchicalTextSplitter;
import de.papenhagen.hierarchical_splitter.core.OpenAITokenCounter;
import de.papenhagen.hierarchical_splitter.core.TokenCounter;
import de.papenhagen.hierarchical_splitter.properties.HierarchicalSplitterProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Auto-configuration for HierarchicalTextSplitter.
 */
@AutoConfiguration
@EnableConfigurationProperties(HierarchicalSplitterProperties.class)
public final class HierarchicalSplitterAutoConfiguration {

    /**
     * Creates the HierarchicalTextSplitter bean.
     *
     * @param properties the properties
     * @return the splitter
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "spring.ai.splitter", name = "enabled", havingValue = "true", matchIfMissing = true)
    @Primary
    public HierarchicalTextSplitter hierarchicalTextSplitter(
            final HierarchicalSplitterProperties properties) {
        return HierarchicalTextSplitter.builder()
                .maxTokens(properties.getMaxTokens())
                .maxCodeLines(properties.getMaxCodeLines())
                .maxTableRows(properties.getMaxTableRows())
                .maxListItems(properties.getMaxListItems())
                .preserveCodeLanguage(properties.isPreserveCodeLanguage())
                .processBlockquotes(properties.isProcessBlockquotes())
                .processHorizontalRules(properties.isProcessHorizontalRules())
                .build();
    }

    /**
     * Creates the default TokenCounter bean.
     *
     * @return the token counter
     */
    @Bean
    @ConditionalOnMissingBean
    public TokenCounter tokenCounter() {
        return new OpenAITokenCounter();
    }
}
