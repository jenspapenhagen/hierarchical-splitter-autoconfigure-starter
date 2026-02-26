package de.papenhagen.hierarchical_splitter.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HierarchicalTextSplitter Builder")
class HierarchicalTextSplitterBuilderTest {

    @Nested
    @DisplayName("default builder")
    class DefaultBuilderTests {

        @Test
        @DisplayName("should create with defaults")
        void shouldCreateWithDefaults() {
            HierarchicalTextSplitter splitter = HierarchicalTextSplitter.builder().build();

            assertThat(splitter.getMaxTokens()).isEqualTo(1000);
            assertThat(splitter.getMaxCodeLines()).isEqualTo(20);
            assertThat(splitter.getMaxTableRows()).isEqualTo(15);
            assertThat(splitter.getMaxListItems()).isEqualTo(15);
            assertThat(splitter.isPreserveCodeLanguage()).isTrue();
            assertThat(splitter.isProcessBlockquotes()).isTrue();
            assertThat(splitter.isProcessHorizontalRules()).isTrue();
            assertThat(splitter.getTokenCounter()).isNotNull();
        }

        @Test
        @DisplayName("should create with custom maxTokens")
        void shouldCreateWithCustomMaxTokens() {
            HierarchicalTextSplitter splitter = HierarchicalTextSplitter.builder()
                    .maxTokens(500)
                    .build();

            assertThat(splitter.getMaxTokens()).isEqualTo(500);
        }

        @Test
        @DisplayName("should create with custom thresholds")
        void shouldCreateWithCustomThresholds() {
            HierarchicalTextSplitter splitter = HierarchicalTextSplitter.builder()
                    .maxCodeLines(30)
                    .maxTableRows(25)
                    .maxListItems(20)
                    .maxBlockquoteLines(10)
                    .build();

            assertThat(splitter.getMaxCodeLines()).isEqualTo(30);
            assertThat(splitter.getMaxTableRows()).isEqualTo(25);
            assertThat(splitter.getMaxListItems()).isEqualTo(20);
            assertThat(splitter.getMaxBlockquoteLines()).isEqualTo(10);
        }

        @Test
        @DisplayName("should create with disabled features")
        void shouldCreateWithDisabledFeatures() {
            HierarchicalTextSplitter splitter = HierarchicalTextSplitter.builder()
                    .preserveCodeLanguage(false)
                    .processBlockquotes(false)
                    .processHorizontalRules(false)
                    .build();

            assertThat(splitter.isPreserveCodeLanguage()).isFalse();
            assertThat(splitter.isProcessBlockquotes()).isFalse();
            assertThat(splitter.isProcessHorizontalRules()).isFalse();
        }

        @Test
        @DisplayName("should use custom token counter")
        void shouldUseCustomTokenCounter() {
            TokenCounter customCounter = text -> text.split("\\s+").length;

            HierarchicalTextSplitter splitter = HierarchicalTextSplitter.builder()
                    .tokenCounter(customCounter)
                    .build();

            assertThat(splitter.getTokenCounter()).isSameAs(customCounter);
        }
    }

    @Nested
    @DisplayName("legacy constructor")
    class LegacyConstructorTests {

        @Test
        @DisplayName("should create with single int parameter")
        void shouldCreateWithSingleIntParameter() {
            HierarchicalTextSplitter splitter = new HierarchicalTextSplitter(500);

            assertThat(splitter.getMaxTokens()).isEqualTo(500);
            assertThat(splitter.getMaxCodeLines()).isEqualTo(20);
            assertThat(splitter.getMaxTableRows()).isEqualTo(15);
            assertThat(splitter.getMaxListItems()).isEqualTo(15);
        }
    }

    @Nested
    @DisplayName("setTokenCounter")
    class SetTokenCounterTests {

        @Test
        @DisplayName("should allow setting token counter after construction")
        void shouldAllowSettingTokenCounterAfterConstruction() {
            HierarchicalTextSplitter splitter = HierarchicalTextSplitter.builder().build();
            TokenCounter customCounter = text -> text.length();

            splitter.setTokenCounter(customCounter);

            assertThat(splitter.getTokenCounter()).isSameAs(customCounter);
        }
    }

    @Nested
    @DisplayName("functional tests with builder")
    class FunctionalTestsWithBuilder {

        @Test
        @DisplayName("should use custom token counter")
        void shouldUseCustomTokenCounter() {
            TokenCounter customCounter = text -> text.split("\\s+").length;

            HierarchicalTextSplitter splitter = HierarchicalTextSplitter.builder()
                    .tokenCounter(customCounter)
                    .build();

            assertThat(splitter.getTokenCounter()).isSameAs(customCounter);
        }

        @Test
        @DisplayName("should process blockquotes when enabled")
        void shouldProcessBlockquotesWhenEnabled() {
            HierarchicalTextSplitter splitter = HierarchicalTextSplitter.builder()
                    .processBlockquotes(true)
                    .build();

            Document doc = Document.builder()
                    .id("1")
                    .text("""
                            Text before
                            > This is a quote
                            > Another line
                            Text after
                            """)
                    .build();

            List<Document> result = splitter.apply(List.of(doc));

            assertThat(result).hasSizeGreaterThanOrEqualTo(1);
            String allText = result.stream().map(Document::getText).reduce((a, b) -> a + "\n" + b).orElse("");
            assertThat(allText).contains("> This is a quote");
        }

        @Test
        @DisplayName("should process horizontal rules when enabled")
        void shouldProcessHorizontalRulesWhenEnabled() {
            HierarchicalTextSplitter splitter = HierarchicalTextSplitter.builder()
                    .processHorizontalRules(true)
                    .build();

            Document doc = Document.builder()
                    .id("1")
                    .text("""
                            Text before
                            ---
                            Text after
                            """)
                    .build();

            List<Document> result = splitter.apply(List.of(doc));

            assertThat(result).hasSize(3);
            assertThat(result.get(1).getText()).isEqualTo("---");
        }

        @Test
        @DisplayName("should skip blockquotes when disabled")
        void shouldSkipBlockquotesWhenDisabled() {
            HierarchicalTextSplitter splitter = HierarchicalTextSplitter.builder()
                    .processBlockquotes(false)
                    .build();

            Document doc = Document.builder()
                    .id("1")
                    .text("""
                            Some text
                            > This is a quote
                            More text
                            """)
                    .build();

            List<Document> result = splitter.apply(List.of(doc));

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getText()).contains("> This is a quote");
        }

        @Test
        @DisplayName("should skip horizontal rules when disabled")
        void shouldSkipHorizontalRulesWhenDisabled() {
            HierarchicalTextSplitter splitter = HierarchicalTextSplitter.builder()
                    .processHorizontalRules(false)
                    .build();

            Document doc = Document.builder()
                    .id("1")
                    .text("""
                            Text before
                            ---
                            Text after
                            """)
                    .build();

            List<Document> result = splitter.apply(List.of(doc));

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getText()).contains("---");
        }
    }
}
