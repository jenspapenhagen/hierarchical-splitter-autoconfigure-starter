# Hierarchical Text Splitter

A Spring Boot starter library for splitting Markdown documents into hierarchical chunks 
suitable for RAG (Retrieval-Augmented Generation) applications.

## Features

- **Hierarchical Splitting** - Preserves heading context in each chunk
- **Markdown Structure Aware** - Respects headings, code blocks, tables, lists, blockquotes
- **Spring Boot Auto-configuration** - Drop-in integration
- **Configurable** - Adjustable thresholds and token limits
- **Pluggable Token Counter** - Use OpenAI, Anthropic, or custom tokenizers

## Dependencies

### Maven

```xml
<dependency>
    <groupId>de.papenhagen</groupId>
    <artifactId>hierarchical-splitter-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'de.papenhagen:hierarchical-splitter-starter:1.0.0'
```

## Requirements

- Java 25
- Spring Boot 4+
- Spring AI 2.0.0-M2+

## Quick Start

```java
@Autowired
private HierarchicalTextSplitter splitter;

public void splitDocument() {
    Document doc = Document.builder()
            .id("source-doc")
            .text("# My Document\n\nContent here...")
            .build();

    List<Document> chunks = splitter.apply(List.of(doc));
}
```

## Configuration

Customize the splitter via `application.yml`:

```yaml
spring:
  ai:
    splitter:
      enabled: true
      max-tokens: 1000
      max-code-lines: 20
      max-table-rows: 15
      max-list-items: 15
      max-blockquote-lines: 15
      preserve-code-language: true
      process-blockquotes: true
      process-horizontal-rules: true
```

### Configuration Properties

| Property | Default | Description |
|----------|---------|-------------|
| `enabled` | `true` | Enable/disable the splitter bean |
| `max-tokens` | `1000` | Maximum tokens per chunk |
| `max-code-lines` | `20` | Max lines for atomic code block chunks |
| `max-table-rows` | `15` | Max rows for atomic table chunks |
| `max-list-items` | `15` | Max items for atomic list chunks |
| `max-blockquote-lines` | `15` | Max lines for atomic blockquote chunks |
| `preserve-code-language` | `true` | Preserve code language identifiers |
| `process-blockquotes` | `true` | Process blockquotes as atomic chunks |
| `process-horizontal-rules` | `true` | Split at horizontal rules |

## Standalone Usage

### Using Constructor (Legacy)

```java
HierarchicalTextSplitter splitter = new HierarchicalTextSplitter(1000);

Document doc = Document.builder()
        .id("source-doc")
        .text(markdownContent)
        .build();

List<Document> chunks = splitter.apply(List.of(doc));
```

### Using Builder (Recommended)

```java
HierarchicalTextSplitter splitter = HierarchicalTextSplitter.builder()
        .maxTokens(1500)
        .maxCodeLines(30)
        .maxTableRows(20)
        .maxListItems(25)
        .processBlockquotes(true)
        .processHorizontalRules(true)
        .build();

Document doc = Document.builder()
        .id("source-doc")
        .text(markdownContent)
        .build();

List<Document> chunks = splitter.apply(List.of(doc));
```

### Using Custom Token Counter

```java
TokenCounter customCounter = text -> text.split("\\s+").length;

HierarchicalTextSplitter splitter = HierarchicalTextSplitter.builder()
        .maxTokens(1000)
        .tokenCounter(customCounter)
        .build();
```

Or use the static helper:

```java
HierarchicalTextSplitter splitter = HierarchicalTextSplitter.builder()
        .maxTokens(1000)
        .tokenCounter(TokenCounter.wordCount())
        .build();
```

## Output Format

Each chunk includes metadata with hierarchical context:

| Field          | Description                                                  |
|----------------|--------------------------------------------------------------|
| `headings`     | List of heading texts from parent hierarchy                  |
| `heading_path` | Human-readable path (e.g., "Getting Started > Installation") |
| `chunk_id`     | SHA-256 hash of heading path + content                       |

## Splitting Rules

### Headings

Headings (h1-h6) create natural chunk boundaries. The current heading path is preserved in chunk metadata:

```markdown
# Title
## Section A
Content under Section A belongs to "Title > Section A"

## Section B
### Subsection
Content here belongs to "Title > Section B > Subsection"
```

### Code Blocks

- **Small code blocks** (≤20 lines by default): Emitted as separate atomic chunks
- **Large code blocks** (>20 lines): Split together with surrounding text

Code blocks are identified by fenced markers (` ``` ` or `~~~`).

### Tables

- **Small tables** (≤15 rows by default): Emitted as separate atomic chunks
- **Large tables** (>15 rows): Split together with surrounding text

Tables are identified by rows matching the `|column1|column2|` pattern.

### Lists

- **Small lists** (≤15 items by default): Emitted as separate atomic chunks
- **Large lists** (>15 items): Split together with surrounding text

Lists are identified by markers (`-`, `*`, `+`, or numbered items like `1.`). Continuation lines (indented content) are included with the list.

### Blockquotes

- **Small blockquotes** (≤15 lines by default): Emitted as separate atomic chunks
- **Large blockquotes** (>15 lines): Split together with surrounding text

Blockquotes are identified by the `>` prefix.

### Horizontal Rules

Lines matching `---`, `***`, or `___` always create chunk boundaries.

### Regular Text

Text is accumulated until the token limit is reached, then split at natural boundaries (paragraphs, list items).

## How It Works

1. The splitter processes Markdown line by line
2. When a heading is encountered, the current buffer is flushed and the heading path is updated
3. Small code blocks, tables, lists, and blockquotes are emitted immediately as atomic chunks
4. Large elements are added to the buffer
5. When `maxTokens` is exceeded, a new chunk is created

This approach ensures that related content stays together while respecting markdown structural boundaries.

## Token Counters

The library includes:

- **OpenAITokenCounter** (default) - Uses jtokkit with cl100k_base encoding
- **TokenCounter.wordCount()** - Simple word count fallback
- Custom token counters via the `TokenCounter` interface
