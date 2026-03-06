package de.papenhagen.hierarchical_splitter.core;

import de.papenhagen.hierarchical_splitter.properties.HierarchicalSplitterProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("HierarchicalTextSplitter")
class HierarchicalTextSplitterTest {

    private HierarchicalTextSplitter splitter;

    @BeforeEach
    void setUp() {
        splitter = HierarchicalTextSplitter.builder()
                .maxTokens(100)
                .build();
    }

    private Document createDocument(String text) {
        return Document.builder()
                .id(UUID.randomUUID().toString())
                .text(text)
                .metadata(Map.of())
                .build();
    }

    private Document createDocument(String text, Map<String, Object> metadata) {
        return Document.builder()
                .id(UUID.randomUUID().toString())
                .text(text)
                .metadata(metadata)
                .build();
    }

    @Nested
    @DisplayName("apply")
    class ApplyTests {

        @Test
        @DisplayName("should return empty list for empty document list")
        void shouldReturnEmptyListForEmptyDocumentList() {
            List<Document> result = splitter.apply(List.of());
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return empty list for null text document")
        void shouldReturnEmptyListForNullTextDocument() {
            Document document = Document.builder()
                    .id("1")
                    .text("")
                    .build();

            List<Document> result = splitter.apply(List.of(document));

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return empty list for blank text document")
        void shouldReturnEmptyListForBlankTextDocument() {
            Document document = createDocument("   \n\t   ");

            List<Document> result = splitter.apply(List.of(document));

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return single chunk for simple text")
        void shouldReturnSingleChunkForSimpleText() {
            String text = "This is a simple paragraph with some text that should fit in one chunk.";
            Document document = createDocument(text);

            List<Document> result = splitter.apply(List.of(document));

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getText()).isEqualTo(text);
        }

        @Test
        @DisplayName("should preserve existing metadata in chunks")
        void shouldPreserveExistingMetadataInChunks() {
            Document document = createDocument("Simple text", Map.of("source", "test", "author", "john"));

            List<Document> result = splitter.apply(List.of(document));

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getMetadata())
                    .containsEntry("source", "test")
                    .containsEntry("author", "john");
        }

        @Test
        @DisplayName("should add hierarchical metadata to chunks")
        void shouldAddHierarchicalMetadataToChunks() {
            String text = """
                    # Heading 1
                    Some content here.
                    """;
            Document document = createDocument(text);

            List<Document> result = splitter.apply(List.of(document));

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getMetadata())
                    .containsKey("headings")
                    .containsKey("heading_path")
                    .containsKey("chunk_id");
        }

        @Test
        @DisplayName("should not split text when token limit not exceeded")
        void shouldNotSplitTextWhenTokenLimitNotExceeded() {
            String text = "word1 word2 word3 word4 word5 word6 word7 word8 word9 word10 word11 word12";
            Document document = createDocument(text);

            List<Document> result = splitter.apply(List.of(document));

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("heading handling")
    class HeadingTests {

        @Test
        @DisplayName("should extract headings and add to metadata")
        void shouldExtractHeadingsAndAddToMetadata() {
            String text = """
                    # Main Heading
                    ## Sub Heading
                    Some content
                    """;
            Document document = createDocument(text);

            List<Document> result = splitter.apply(List.of(document));

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getMetadata().get("headings"))
                    .asList()
                    .containsExactly("Main Heading", "Sub Heading");
        }

        @Test
        @DisplayName("should clear sub-headings when new heading is at same or higher level")
        void shouldClearSubHeadingsWhenNewHeadingAtSameOrHigherLevel() {
            String text = """
                    # Heading 1
                    ## Sub Heading
                    ### Deep Heading
                    Content
                    # New Heading 1
                    Content after new heading
                    """;
            Document document = createDocument(text);

            List<Document> result = splitter.apply(List.of(document));

            assertThat(result).hasSize(2);
            assertThat(result.get(1).getMetadata().get("headings"))
                    .asList()
                    .containsExactly("New Heading 1");
        }

        @Test
        @DisplayName("should create heading_path from headings")
        void shouldCreateHeadingPathFromHeadings() {
            String text = """
                    # Getting Started
                    ## Installation
                    Content here
                    """;
            Document document = createDocument(text);

            List<Document> result = splitter.apply(List.of(document));

            assertThat(result.get(0).getMetadata().get("heading_path"))
                    .isEqualTo("Getting Started > Installation");
        }
    }

    @Nested
    @DisplayName("code block handling")
    class CodeBlockTests {

        @Test
        @DisplayName("should process code blocks")
        void shouldProcessCodeBlocks() {
            String text = """
                    Some text
                    ```java
                    public class Test {
                    }
                    ```
                    More text
                    """;
            Document document = createDocument(text);

            List<Document> result = splitter.apply(List.of(document));

            assertThat(result).hasSizeGreaterThanOrEqualTo(1);
            String allText = result.stream().map(Document::getText).reduce((a, b) -> a + "\n" + b).orElse("");
            assertThat(allText).contains("```java");
        }

        @Test
        @DisplayName("should handle large code blocks")
        void shouldHandleLargeCodeBlocks() {
            StringBuilder text = new StringBuilder("Text before\n```java\n");
            for (int i = 0; i < 25; i++) {
                text.append("line ").append(i).append("\n");
            }
            text.append("```\nText after");
            Document document = createDocument(text.toString());

            List<Document> result = splitter.apply(List.of(document));

            assertThat(result).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("table handling")
    class TableTests {

        @Test
        @DisplayName("should process tables")
        void shouldProcessTables() {
            String text = """
                    Some text
                    | Column 1 | Column 2 |
                    |----------|----------|
                    | Value 1  | Value 2  |
                    More text
                    """;
            Document document = createDocument(text);

            List<Document> result = splitter.apply(List.of(document));

            assertThat(result).hasSizeGreaterThanOrEqualTo(1);
            String allText = result.stream().map(Document::getText).reduce((a, b) -> a + "\n" + b).orElse("");
            assertThat(allText).contains("| Column 1 |");
        }

        @Test
        @DisplayName("should handle large tables")
        void shouldHandleLargeTables() {
            StringBuilder text = new StringBuilder("Text before\n| Col1 | Col2 |\n|------|------|\n");
            for (int i = 0; i < 20; i++) {
                text.append("| val").append(i).append(" | val").append(i).append(" |\n");
            }
            text.append("Text after");
            Document document = createDocument(text.toString());

            List<Document> result = splitter.apply(List.of(document));

            assertThat(result).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("list handling")
    class ListTests {

        @Test
        @DisplayName("should process lists")
        void shouldProcessLists() {
            String text = """
                    Some text
                    - Item 1
                    - Item 2
                    - Item 3
                    More text
                    """;
            Document document = createDocument(text);

            List<Document> result = splitter.apply(List.of(document));

            assertThat(result).hasSizeGreaterThanOrEqualTo(1);
            String allText = result.stream().map(Document::getText).reduce((a, b) -> a + "\n" + b).orElse("");
            assertThat(allText).contains("- Item 1");
        }

        @Test
        @DisplayName("should handle large lists")
        void shouldHandleLargeLists() {
            StringBuilder text = new StringBuilder("Text before\n");
            for (int i = 1; i <= 20; i++) {
                text.append("- Item ").append(i).append("\n");
            }
            text.append("Text after");
            Document document = createDocument(text.toString());

            List<Document> result = splitter.apply(List.of(document));

            assertThat(result).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("chunk ID generation")
    class ChunkIdTests {

        @Test
        @DisplayName("should generate unique chunk IDs based on heading path and content")
        void shouldGenerateUniqueChunkIdsBasedOnHeadingPathAndContent() {
            String text1 = "# Heading\nContent A";
            String text2 = "# Heading\nContent B";
            Document document1 = createDocument(text1);
            Document document2 = createDocument(text2);

            List<Document> result1 = splitter.apply(List.of(document1));
            List<Document> result2 = splitter.apply(List.of(document2));

            String chunkId1 = result1.get(0).getId();
            String chunkId2 = result2.get(0).getId();

            assertThat(chunkId1).isNotEqualTo(chunkId2);
        }

        @Test
        @DisplayName("should generate same chunk ID for same content and heading")
        void shouldGenerateSameChunkIdForSameContentAndHeading() {
            String text = "# Heading\nContent";
            Document document1 = createDocument(text);
            Document document2 = createDocument(text);

            List<Document> result1 = splitter.apply(List.of(document1));
            List<Document> result2 = splitter.apply(List.of(document2));

            assertThat(result1.get(0).getId()).isEqualTo(result2.get(0).getId());
        }

        @Test
        @DisplayName("should use chunk_id in metadata")
        void shouldUseChunkIdInMetadata() {
            String text = "# Heading\nSome content";
            Document document = createDocument(text);

            List<Document> result = splitter.apply(List.of(document));

            assertThat(result.get(0).getMetadata().get("chunk_id"))
                    .isEqualTo(result.get(0).getId());
        }
    }

    @Nested
    @DisplayName("multiple documents")
    class MultipleDocumentsTests {

        @Test
        @DisplayName("should process multiple documents")
        void shouldProcessMultipleDocuments() {
            Document doc1 = createDocument("Document 1 content");
            Document doc2 = createDocument("Document 2 content");

            List<Document> result = splitter.apply(List.of(doc1, doc2));

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("should maintain separate heading contexts per document")
        void shouldMaintainSeparateHeadingContextsPerDocument() {
            Document doc1 = createDocument("# Heading 1\nContent 1");
            Document doc2 = createDocument("# Heading 2\nContent 2");

            List<Document> result = splitter.apply(List.of(doc1, doc2));

            assertThat(result.get(0).getMetadata().get("heading_path")).isEqualTo("Heading 1");
            assertThat(result.get(1).getMetadata().get("heading_path")).isEqualTo("Heading 2");
        }
    }

    @Nested
    @DisplayName("edge cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle document with headings and content")
        void shouldHandleDocumentWithHeadingsAndContent() {
            String text = "# Heading 1\nContent\n# Heading 2\n### Sub";
            Document document = createDocument(text);

            List<Document> result = splitter.apply(List.of(document));

            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("should handle fenced code blocks with tilde")
        void shouldHandleFencedCodeBlocksWithTilde() {
            String text = """
                    ~~~
                    code content
                    ~~~
                    """;
            Document document = createDocument(text);

            List<Document> result = splitter.apply(List.of(document));

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getText()).contains("~~~");
        }

        @Test
        @DisplayName("should preserve score from parent document")
        void shouldPreserveScoreFromParentDocument() {
            Document document = Document.builder()
                    .id("1")
                    .text("Some text")
                    .score(0.95)
                    .build();

            List<Document> result = splitter.apply(List.of(document));

            assertThat(result.get(0).getScore()).isEqualTo(0.95);
        }
    }
}
