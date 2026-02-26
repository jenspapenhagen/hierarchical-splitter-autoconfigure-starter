package de.papenhagen.hierarchical_splitter.properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HierarchicalSplitterProperties")
class HierarchicalSplitterPropertiesTest {

    private HierarchicalSplitterProperties properties;

    @BeforeEach
    void setUp() {
        properties = new HierarchicalSplitterProperties();
    }

    @Nested
    @DisplayName("default values")
    class DefaultValuesTests {

        @Test
        @DisplayName("should have default maxTokens of 1000")
        void shouldHaveDefaultMaxTokens() {
            assertThat(properties.getMaxTokens()).isEqualTo(1000);
        }

        @Test
        @DisplayName("should have default maxCodeLines of 20")
        void shouldHaveDefaultMaxCodeLines() {
            assertThat(properties.getMaxCodeLines()).isEqualTo(20);
        }

        @Test
        @DisplayName("should have default maxTableRows of 15")
        void shouldHaveDefaultMaxTableRows() {
            assertThat(properties.getMaxTableRows()).isEqualTo(15);
        }

        @Test
        @DisplayName("should have default maxListItems of 15")
        void shouldHaveDefaultMaxListItems() {
            assertThat(properties.getMaxListItems()).isEqualTo(15);
        }

        @Test
        @DisplayName("should have preserveCodeLanguage enabled by default")
        void shouldHavePreserveCodeLanguageEnabledByDefault() {
            assertThat(properties.isPreserveCodeLanguage()).isTrue();
        }

        @Test
        @DisplayName("should have processBlockquotes enabled by default")
        void shouldHaveProcessBlockquotesEnabledByDefault() {
            assertThat(properties.isProcessBlockquotes()).isTrue();
        }

        @Test
        @DisplayName("should have processHorizontalRules enabled by default")
        void shouldHaveProcessHorizontalRulesEnabledByDefault() {
            assertThat(properties.isProcessHorizontalRules()).isTrue();
        }

        @Test
        @DisplayName("should be enabled by default")
        void shouldBeEnabledByDefault() {
            assertThat(properties.isEnabled()).isTrue();
        }
    }

    @Nested
    @DisplayName("setters")
    class SettersTests {

        @Test
        @DisplayName("should set maxTokens")
        void shouldSetMaxTokens() {
            properties.setMaxTokens(500);
            assertThat(properties.getMaxTokens()).isEqualTo(500);
        }

        @Test
        @DisplayName("should set maxCodeLines")
        void shouldSetMaxCodeLines() {
            properties.setMaxCodeLines(30);
            assertThat(properties.getMaxCodeLines()).isEqualTo(30);
        }

        @Test
        @DisplayName("should set maxTableRows")
        void shouldSetMaxTableRows() {
            properties.setMaxTableRows(25);
            assertThat(properties.getMaxTableRows()).isEqualTo(25);
        }

        @Test
        @DisplayName("should set maxListItems")
        void shouldSetMaxListItems() {
            properties.setMaxListItems(20);
            assertThat(properties.getMaxListItems()).isEqualTo(20);
        }

        @Test
        @DisplayName("should set preserveCodeLanguage")
        void shouldSetPreserveCodeLanguage() {
            properties.setPreserveCodeLanguage(false);
            assertThat(properties.isPreserveCodeLanguage()).isFalse();
        }

        @Test
        @DisplayName("should set processBlockquotes")
        void shouldSetProcessBlockquotes() {
            properties.setProcessBlockquotes(false);
            assertThat(properties.isProcessBlockquotes()).isFalse();
        }

        @Test
        @DisplayName("should set processHorizontalRules")
        void shouldSetProcessHorizontalRules() {
            properties.setProcessHorizontalRules(false);
            assertThat(properties.isProcessHorizontalRules()).isFalse();
        }

        @Test
        @DisplayName("should set enabled")
        void shouldSetEnabled() {
            properties.setEnabled(false);
            assertThat(properties.isEnabled()).isFalse();
        }
    }

    @Nested
    @DisplayName("builder")
    class BuilderTests {

        @Test
        @DisplayName("should create properties with custom values")
        void shouldCreatePropertiesWithCustomValues() {
            HierarchicalSplitterProperties props = HierarchicalSplitterProperties.builder()
                    .maxTokens(2000)
                    .maxCodeLines(40)
                    .maxTableRows(30)
                    .maxListItems(25)
                    .preserveCodeLanguage(false)
                    .processBlockquotes(false)
                    .processHorizontalRules(false)
                    .enabled(false)
                    .build();

            assertThat(props.getMaxTokens()).isEqualTo(2000);
            assertThat(props.getMaxCodeLines()).isEqualTo(40);
            assertThat(props.getMaxTableRows()).isEqualTo(30);
            assertThat(props.getMaxListItems()).isEqualTo(25);
            assertThat(props.isPreserveCodeLanguage()).isFalse();
            assertThat(props.isProcessBlockquotes()).isFalse();
            assertThat(props.isProcessHorizontalRules()).isFalse();
            assertThat(props.isEnabled()).isFalse();
        }

        @Test
        @DisplayName("should create properties with default values")
        void shouldCreatePropertiesWithDefaultValues() {
            HierarchicalSplitterProperties props = HierarchicalSplitterProperties.builder().build();

            assertThat(props.getMaxTokens()).isEqualTo(1000);
            assertThat(props.getMaxCodeLines()).isEqualTo(20);
            assertThat(props.getMaxTableRows()).isEqualTo(15);
            assertThat(props.getMaxListItems()).isEqualTo(15);
            assertThat(props.isPreserveCodeLanguage()).isTrue();
            assertThat(props.isProcessBlockquotes()).isTrue();
            assertThat(props.isProcessHorizontalRules()).isTrue();
            assertThat(props.isEnabled()).isTrue();
        }

        @Test
        @DisplayName("should convert properties to builder")
        void shouldConvertPropertiesToBuilder() {
            properties.setMaxTokens(1500);
            properties.setMaxCodeLines(25);

            HierarchicalSplitterProperties copy = properties.toBuilder().build();

            assertThat(copy.getMaxTokens()).isEqualTo(1500);
            assertThat(copy.getMaxCodeLines()).isEqualTo(25);
        }
    }

    @Nested
    @DisplayName("equals and hashCode")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("should be equal when all properties match")
        void shouldBeEqualWhenAllPropertiesMatch() {
            HierarchicalSplitterProperties props1 = HierarchicalSplitterProperties.builder()
                    .maxTokens(1000)
                    .build();
            HierarchicalSplitterProperties props2 = HierarchicalSplitterProperties.builder()
                    .maxTokens(1000)
                    .build();

            assertThat(props1).isEqualTo(props2);
            assertThat(props1.hashCode()).isEqualTo(props2.hashCode());
        }

        @Test
        @DisplayName("should not be equal when properties differ")
        void shouldNotBeEqualWhenPropertiesDiffer() {
            HierarchicalSplitterProperties props1 = HierarchicalSplitterProperties.builder()
                    .maxTokens(1000)
                    .build();
            HierarchicalSplitterProperties props2 = HierarchicalSplitterProperties.builder()
                    .maxTokens(2000)
                    .build();

            assertThat(props1).isNotEqualTo(props2);
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("should return string representation")
        void shouldReturnStringRepresentation() {
            properties.setMaxTokens(500);

            String str = properties.toString();

            assertThat(str).contains("maxTokens=500");
        }
    }
}
