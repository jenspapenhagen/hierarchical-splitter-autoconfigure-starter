package de.papenhagen.hierarchical_splitter.core;

import java.io.BufferedReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;

/**
 * Splits markdown documents into hierarchical chunks suitable for RAG applications.
 */
public final class HierarchicalTextSplitter extends TextSplitter {

    private static final Pattern HEADING = Pattern.compile("^(#{1,6})\\s+(.*)$");
    private static final Pattern LIST_ITEM = Pattern.compile("^\\s*([-*+]|\\d+\\.)\\s+(.+)$");
    private static final Pattern LIST_CONTINUATION = Pattern.compile("^\\s{2,}.+$");
    private static final Pattern TABLE_ROW = Pattern.compile("^\\s*\\|.*\\|\\s*$");
    private static final Pattern TABLE_SEPARATOR = Pattern.compile("^\\s*\\|?\\s*:?[-]+:?.*\\|?\\s*$");
    private static final Pattern FENCE = Pattern.compile("^(```|~~~)");
    private static final Pattern BLOCKQUOTE = Pattern.compile("^>\\s*(.*)$");
    private static final Pattern HORIZONTAL_RULE = Pattern.compile("^[-*_]{3,}\\s*$");

    private static final int DEFAULT_MAX_TOKENS = 1000;
    private static final int DEFAULT_MAX_CODE_LINES = 20;
    private static final int DEFAULT_MAX_TABLE_ROWS = 15;
    private static final int DEFAULT_MAX_LIST_ITEMS = 15;
    private static final int DEFAULT_MAX_BLOCKQUOTE_LINES = 15;
    private static final int READER_MARK_LIMIT = 10000;

    private final int maxTokens;
    private final int maxCodeLines;
    private final int maxTableRows;
    private final int maxListItems;
    private final int maxBlockquoteLines;
    private final boolean preserveCodeLanguage;
    private final boolean processBlockquotes;
    private final boolean processHorizontalRules;
    private TokenCounter tokenCounter;

    private HierarchicalTextSplitter(final Builder builder) {
        this.maxTokens = builder.maxTokens;
        this.maxCodeLines = builder.maxCodeLines;
        this.maxTableRows = builder.maxTableRows;
        this.maxListItems = builder.maxListItems;
        this.maxBlockquoteLines = builder.maxBlockquoteLines;
        this.preserveCodeLanguage = builder.preserveCodeLanguage;
        this.processBlockquotes = builder.processBlockquotes;
        this.processHorizontalRules = builder.processHorizontalRules;
        this.tokenCounter = builder.tokenCounter != null
                ? builder.tokenCounter
                : new OpenAITokenCounter();
    }

    /**
     * Creates a HierarchicalTextSplitter with default settings.
     *
     * @param maxTokens the maximum number of tokens per chunk
     */
    public HierarchicalTextSplitter(final int maxTokens) {
        this.maxTokens = maxTokens;
        this.maxCodeLines = DEFAULT_MAX_CODE_LINES;
        this.maxTableRows = DEFAULT_MAX_TABLE_ROWS;
        this.maxListItems = DEFAULT_MAX_LIST_ITEMS;
        this.maxBlockquoteLines = DEFAULT_MAX_BLOCKQUOTE_LINES;
        this.preserveCodeLanguage = true;
        this.processBlockquotes = true;
        this.processHorizontalRules = true;
        this.tokenCounter = new OpenAITokenCounter();
    }

    /**
     * Creates a new Builder instance.
     *
     * @return a new Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Sets the token counter to use for counting tokens.
     *
     * @param tokenCounter the token counter
     */
    public void setTokenCounter(final TokenCounter tokenCounter) {
        this.tokenCounter = tokenCounter;
    }

    @Override
    protected List<String> splitText(final String text) {
        return List.of(text);
    }

    @Override
    public List<Document> apply(final List<Document> documents) {
        final List<Document> result = new ArrayList<>();

        for (final Document document : documents) {
            result.addAll(splitStreaming(document));
        }

        return result;
    }

    private List<Document> splitStreaming(final Document document) {
        final List<Document> chunks = new ArrayList<>();
        final TreeMap<Integer, String> headings = new TreeMap<>();

        final StringBuilder stringBuilder = new StringBuilder();
        int bufferTokens = 0;

        final String documentText = document.getText();

        if (documentText == null || documentText.isBlank()) {
            return List.of();
        }

        try (final BufferedReader reader = new BufferedReader(new StringReader(documentText))) {

            String line;
            boolean inCodeBlock = false;
            String fenceMarker = null;

            final List<String> codeBuffer = new ArrayList<>();
            final List<String> tableBuffer = new ArrayList<>();
            final List<String> listBuffer = new ArrayList<>();
            final List<String> blockquoteBuffer = new ArrayList<>();

            while ((line = reader.readLine()) != null) {

                if (inCodeBlock) {
                    codeBuffer.add(line);
                    if (line.startsWith(fenceMarker)) {
                        inCodeBlock = false;

                        if (codeBuffer.size() - 2 <= maxCodeLines) {
                            flushBufferIfNeeded(chunks, document, headings,
                                    stringBuilder);
                            emitChunk(chunks, document, headings,
                                    String.join("\n", codeBuffer));
                        } else {
                            appendToBuffer(stringBuilder, codeBuffer);
                        }
                        codeBuffer.clear();
                    }
                    continue;
                }

                final Matcher fenceMatcher = FENCE.matcher(line);
                if (fenceMatcher.matches()) {
                    inCodeBlock = true;
                    fenceMarker = fenceMatcher.group(1);
                    codeBuffer.add(line);
                    continue;
                }

                if (processBlockquotes) {
                    final Matcher blockquoteMatcher = BLOCKQUOTE.matcher(line);
                    if (blockquoteMatcher.matches()) {
                        blockquoteBuffer.add(blockquoteMatcher.group(1));

                        reader.mark(READER_MARK_LIMIT);
                        String next;
                        while ((next = reader.readLine()) != null) {
                            final Matcher nextMatcher = BLOCKQUOTE.matcher(next);
                            if (nextMatcher.matches()) {
                                blockquoteBuffer.add(nextMatcher.group(1));
                            } else {
                                break;
                            }
                        }

                        if (next != null) {
                            reader.reset();
                        }

                        if (blockquoteBuffer.size() <= maxBlockquoteLines) {
                            flushBufferIfNeeded(chunks, document, headings,
                                    stringBuilder);
                            emitChunk(chunks, document, headings,
                                    blockquoteBuffer.stream()
                                            .map(s -> "> " + s)
                                            .reduce((a, b) -> a + "\n" + b)
                                            .orElse(""));
                        } else {
                            appendToBuffer(stringBuilder, blockquoteBuffer.stream()
                                    .map(s -> "> " + s)
                                    .toList());
                        }

                        blockquoteBuffer.clear();
                        continue;
                    }
                }

                if (processHorizontalRules) {
                    final Matcher hrMatcher = HORIZONTAL_RULE.matcher(line);
                    if (hrMatcher.matches()) {
                        flushBufferIfNeeded(chunks, document, headings,
                                stringBuilder);
                        emitChunk(chunks, document, headings, line);
                        continue;
                    }
                }

                final Matcher headingMatcher = HEADING.matcher(line);
                if (headingMatcher.matches()) {
                    flushBufferIfNeeded(chunks, document, headings,
                            stringBuilder);

                    final int level = headingMatcher.group(1).length();
                    final String text = headingMatcher.group(2).trim();

                    headings.tailMap(level, true).clear();
                    headings.put(level, text);
                    continue;
                }

                final Matcher tableMatcher = TABLE_ROW.matcher(line);
                if (tableMatcher.matches()) {
                    tableBuffer.add(line);
                    reader.mark(READER_MARK_LIMIT);

                    String next;
                    while ((next = reader.readLine()) != null
                            && (TABLE_ROW.matcher(next).matches()
                            || TABLE_SEPARATOR.matcher(next).matches())) {
                        tableBuffer.add(next);
                    }

                    if (next != null) {
                        reader.reset();
                    }

                    if (tableBuffer.size() <= maxTableRows) {
                        flushBufferIfNeeded(chunks, document, headings,
                                stringBuilder);
                        emitChunk(chunks, document, headings,
                                String.join("\n", tableBuffer));
                    } else {
                        appendToBuffer(stringBuilder, tableBuffer);
                    }

                    tableBuffer.clear();
                    continue;
                }

                final Matcher listMatcher = LIST_ITEM.matcher(line);
                final boolean isListItem = listMatcher.matches();
                final boolean isContinuation = !listBuffer.isEmpty()
                        && LIST_CONTINUATION.matcher(line).matches();

                if (isListItem || isContinuation) {
                    if (listBuffer.isEmpty()) {
                        reader.mark(READER_MARK_LIMIT);
                        listBuffer.add(line);

                        String next;
                        while ((next = reader.readLine()) != null
                                && (LIST_ITEM.matcher(next).matches()
                                || LIST_CONTINUATION.matcher(next).matches())) {
                            listBuffer.add(next);
                        }

                        if (next != null) {
                            reader.reset();
                        }

                        if (listBuffer.size() <= maxListItems) {
                            flushBufferIfNeeded(chunks, document, headings,
                                    stringBuilder);
                            emitChunk(chunks, document, headings,
                                    String.join("\n", listBuffer));
                        } else {
                            appendToBuffer(stringBuilder, listBuffer);
                        }

                        listBuffer.clear();
                    } else {
                        listBuffer.add(line);
                    }
                    continue;
                }

                final int lineTokens = tokenCounter.count(line);

                if (bufferTokens + lineTokens > maxTokens) {
                    emitChunk(chunks, document, headings, stringBuilder.toString());
                    stringBuilder.setLength(0);
                    bufferTokens = 0;
                }

                stringBuilder.append(line).append("\n");
                bufferTokens += lineTokens;
            }

            if (!stringBuilder.isEmpty()) {
                emitChunk(chunks, document, headings, stringBuilder.toString());
            }

        } catch (Exception e) {
            throw new RuntimeException("Streaming split failed", e);
        }

        return chunks;
    }

    private void flushBufferIfNeeded(final List<Document> chunks,
                                     final Document document,
                                     final TreeMap<Integer, String> headings,
                                     final StringBuilder stringBuilder) {
        if (!stringBuilder.isEmpty()) {
            emitChunk(chunks, document, headings, stringBuilder.toString());
            stringBuilder.setLength(0);
        }
    }

    private void appendToBuffer(final StringBuilder stringBuilder,
                                final List<String> lines) {
        for (final String line : lines) {
            stringBuilder.append(line).append("\n");
        }
    }

    private void emitChunk(final List<Document> chunks,
                           final Document parent,
                           final TreeMap<Integer, String> headings,
                           final String text) {

        if (text == null || text.isBlank()) {
            return;
        }

        final String trimmed = text.trim();
        final String headingPath = String.join(" > ", headings.values());
        final String chunkId = sha256(headingPath + "\n" + trimmed);

        final Map<String, Object> metadata = new LinkedHashMap<>(parent.getMetadata());
        metadata.put("headings", new ArrayList<>(headings.values()));
        metadata.put("heading_path", headingPath);
        metadata.put("chunk_id", chunkId);

        chunks.add(Document.builder()
                .id(chunkId)
                .text(trimmed)
                .metadata(metadata)
                .score(parent.getScore())
                .build());
    }

    private static String sha256(final String input) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            final StringBuilder stringBuilder = new StringBuilder();
            for (byte b : hash) {
                stringBuilder.append(String.format("%02x", b));
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
     * Returns the maximum number of lines in a code block.
     *
     * @return the max code lines
     */
    public int getMaxCodeLines() {
        return maxCodeLines;
    }

    /**
     * Returns the maximum number of rows in a table.
     *
     * @return the max table rows
     */
    public int getMaxTableRows() {
        return maxTableRows;
    }

    /**
     * Returns the maximum number of list items.
     *
     * @return the max list items
     */
    public int getMaxListItems() {
        return maxListItems;
    }

    /**
     * Returns the maximum number of lines in a blockquote.
     *
     * @return the max blockquote lines
     */
    public int getMaxBlockquoteLines() {
        return maxBlockquoteLines;
    }

    /**
     * Returns whether to preserve code language annotations.
     *
     * @return true if preserving code language
     */
    public boolean isPreserveCodeLanguage() {
        return preserveCodeLanguage;
    }

    /**
     * Returns whether to process blockquotes.
     *
     * @return true if processing blockquotes
     */
    public boolean isProcessBlockquotes() {
        return processBlockquotes;
    }

    /**
     * Returns whether to process horizontal rules.
     *
     * @return true if processing horizontal rules
     */
    public boolean isProcessHorizontalRules() {
        return processHorizontalRules;
    }

    /**
     * Returns the token counter.
     *
     * @return the token counter
     */
    public TokenCounter getTokenCounter() {
        return tokenCounter;
    }

    /**
     * Builder for HierarchicalTextSplitter.
     */
    public static final class Builder {
        private int maxTokens = DEFAULT_MAX_TOKENS;
        private int maxCodeLines = DEFAULT_MAX_CODE_LINES;
        private int maxTableRows = DEFAULT_MAX_TABLE_ROWS;
        private int maxListItems = DEFAULT_MAX_LIST_ITEMS;
        private int maxBlockquoteLines = DEFAULT_MAX_BLOCKQUOTE_LINES;
        private boolean preserveCodeLanguage = true;
        private boolean processBlockquotes = true;
        private boolean processHorizontalRules = true;
        private TokenCounter tokenCounter;

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
         * Sets the maximum number of lines in a blockquote.
         *
         * @param maxBlockquoteLines the max blockquote lines
         * @return this builder
         */
        public Builder maxBlockquoteLines(final int maxBlockquoteLines) {
            this.maxBlockquoteLines = maxBlockquoteLines;
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
         * Sets the token counter to use.
         *
         * @param tokenCounter the token counter
         * @return this builder
         */
        public Builder tokenCounter(final TokenCounter tokenCounter) {
            this.tokenCounter = tokenCounter;
            return this;
        }

        /**
         * Builds the HierarchicalTextSplitter.
         *
         * @return the splitter
         */
        public HierarchicalTextSplitter build() {
            return new HierarchicalTextSplitter(this);
        }
    }
}
