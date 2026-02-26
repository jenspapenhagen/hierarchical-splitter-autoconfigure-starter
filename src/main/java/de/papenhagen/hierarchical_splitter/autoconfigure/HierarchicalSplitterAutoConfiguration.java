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

@AutoConfiguration
@EnableConfigurationProperties(HierarchicalSplitterProperties.class)
public class HierarchicalSplitterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @Primary
    public HierarchicalTextSplitter hierarchicalTextSplitter(HierarchicalSplitterProperties properties) {
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

    @Bean
    @ConditionalOnMissingBean
    public TokenCounter tokenCounter() {
        return new OpenAITokenCounter();
    }
}
