package de.papenhagen.hierarchical_splitter.autoconfigure;

import de.papenhagen.hierarchical_splitter.core.HierarchicalTextSplitter;
import de.papenhagen.hierarchical_splitter.core.TokenCounter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class HierarchicalSplitterAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(HierarchicalSplitterAutoConfiguration.class));

    @Test
    void autoConfigurationLoads() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(HierarchicalTextSplitter.class);
            assertThat(context).hasSingleBean(TokenCounter.class);
        });
    }

    @Test
    void conditionalOnMissingBeanAllowsUserOverride() {
        contextRunner.withBean(HierarchicalTextSplitter.class, () -> HierarchicalTextSplitter.builder()
                .maxTokens(500)
                .build())
                .run(context -> {
                    HierarchicalTextSplitter splitter = context.getBean(HierarchicalTextSplitter.class);
                    assertThat(splitter.getMaxTokens()).isEqualTo(500);
                });
    }

    @Test
    void conditionalOnPropertyDisabled() {
        contextRunner.withPropertyValues("spring.ai.splitter.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(HierarchicalTextSplitter.class);
                    assertThat(context).hasSingleBean(TokenCounter.class);
                });
    }

    @Test
    void conditionalOnPropertyEnabled() {
        contextRunner.withPropertyValues("spring.ai.splitter.enabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(HierarchicalTextSplitter.class);
                });
    }

    @Test
    void defaultEnabledWhenPropertyNotSet() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(HierarchicalTextSplitter.class);
        });
    }

    @Test
    void tokenCounterCanBeOverridden() {
        contextRunner.withBean(TokenCounter.class, () -> text -> 0)
                .run(context -> {
                    TokenCounter counter = context.getBean(TokenCounter.class);
                    assertThat(counter.count("test")).isEqualTo(0);
                });
    }

    @Test
    void hierarchicalTextSplitterHasDefaultConfiguration() {
        contextRunner.run(context -> {
            HierarchicalTextSplitter splitter = context.getBean(HierarchicalTextSplitter.class);
            assertThat(splitter.getMaxTokens()).isEqualTo(1000);
            assertThat(splitter.getMaxCodeLines()).isEqualTo(20);
            assertThat(splitter.getMaxTableRows()).isEqualTo(15);
            assertThat(splitter.getMaxListItems()).isEqualTo(15);
        });
    }

    @Test
    void hierarchicalTextSplitterRespectsCustomConfiguration() {
        contextRunner.withPropertyValues(
                "spring.ai.splitter.max-tokens=500",
                "spring.ai.splitter.max-code-lines=10",
                "spring.ai.splitter.max-table-rows=20",
                "spring.ai.splitter.max-list-items=25"
        ).run(context -> {
            HierarchicalTextSplitter splitter = context.getBean(HierarchicalTextSplitter.class);
            assertThat(splitter.getMaxTokens()).isEqualTo(500);
            assertThat(splitter.getMaxCodeLines()).isEqualTo(10);
            assertThat(splitter.getMaxTableRows()).isEqualTo(20);
            assertThat(splitter.getMaxListItems()).isEqualTo(25);
        });
    }

    @Test
    void hierarchicalTextSplitterRespectsBooleanConfiguration() {
        contextRunner.withPropertyValues(
                "spring.ai.splitter.preserve-code-language=false",
                "spring.ai.splitter.process-blockquotes=false",
                "spring.ai.splitter.process-horizontal-rules=false"
        ).run(context -> {
            HierarchicalTextSplitter splitter = context.getBean(HierarchicalTextSplitter.class);
            assertThat(splitter.isPreserveCodeLanguage()).isFalse();
            assertThat(splitter.isProcessBlockquotes()).isFalse();
            assertThat(splitter.isProcessHorizontalRules()).isFalse();
        });
    }
}
