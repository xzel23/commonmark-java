package org.commonmark.ext.gfm.tables.internal;

import org.commonmark.ext.gfm.tables.*;
import org.commonmark.node.Block;
import org.commonmark.node.Node;
import org.commonmark.node.SourceSpan;
import org.commonmark.parser.InlineParser;
import org.commonmark.parser.block.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class TableBlockParser extends AbstractBlockParser {

    private static String COL = "\\s*:?-{3,}:?\\s*";
    private static Pattern TABLE_HEADER_SEPARATOR = Pattern.compile(
            // For single column, require at least one pipe, otherwise it's ambiguous with setext headers
            "\\|" + COL + "\\|?\\s*" + "|" +
                    COL + "\\|\\s*" + "|" +
                    "\\|?" + "(?:" + COL + "\\|)+" + COL + "\\|?\\s*");

    private final TableBlock block = new TableBlock();
    private final List<CharSequence> rowLines = new ArrayList<>();

    private TableBlockParser(CharSequence headerLine) {
        rowLines.add(headerLine);
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public BlockContinue tryContinue(ParserState state) {
        if (state.getLine().toString().contains("|")) {
            addSourceSpan(SourceSpan.of(state.getLineIndex() + 1, state.getNextNonSpaceIndex() + 1, state.getLine().length()));
            return BlockContinue.atIndex(state.getNextNonSpaceIndex());
        } else {
            return BlockContinue.none();
        }
    }

    @Override
    public void addLine(CharSequence line) {
        rowLines.add(line);
    }

    @Override
    public void parseInlines(InlineParser inlineParser) {
        Node section = new TableHead();
        section.setSourceSpans(Collections.singletonList(getSourceSpans().get(0)));
        block.appendChild(section);

        int separatorLineIndex = 1;
        CharSequence separatorLine = rowLines.get(separatorLineIndex);
        List<TableCell.Alignment> alignments = parseAlignment(separatorLine);

        int headerColumns = -1;
        boolean header = true;
        for (int rowIndex = 0; rowIndex < rowLines.size(); rowIndex++) {
            if (rowIndex == separatorLineIndex) {
                // Separator line doesn't result in a node in the document, skip
                continue;
            }

            CharSequence rowLine = rowLines.get(rowIndex);
            SourceSpan rowSpan = getSourceSpans().get(rowIndex);

            List<CellSource> cells = split(rowLine, rowSpan.getLineNumber(), rowSpan.getFirstColumn());

            TableRow tableRow = new TableRow();
            tableRow.setSourceSpans(Collections.singletonList(rowSpan));

            if (headerColumns == -1) {
                headerColumns = cells.size();
            }

            // Body can not have more columns than head
            for (int i = 0; i < headerColumns; i++) {
                CellSource cell = i < cells.size() ? cells.get(i) : new CellSource("", null);
                TableCell.Alignment alignment = i < alignments.size() ? alignments.get(i) : null;
                TableCell tableCell = new TableCell();
                tableCell.setHeader(header);
                tableCell.setAlignment(alignment);
                if (cell.sourceSpan != null) {
                    tableCell.setSourceSpans(Collections.singletonList(cell.sourceSpan));
                }
                inlineParser.parse(cell.content, tableCell);
                tableRow.appendChild(tableCell);
            }

            section.appendChild(tableRow);

            if (header) {
                // Format allows only one row in head
                header = false;
                section = new TableBody();
                section.setSourceSpans(getSourceSpans().subList(2, getSourceSpans().size()));
                block.appendChild(section);
            }
        }
    }

    private static List<TableCell.Alignment> parseAlignment(CharSequence separatorLine) {
        List<CellSource> parts = split(separatorLine, 0, 0);
        List<TableCell.Alignment> alignments = new ArrayList<>();
        for (CellSource part : parts) {
            String trimmed = part.content.trim();
            boolean left = trimmed.startsWith(":");
            boolean right = trimmed.endsWith(":");
            TableCell.Alignment alignment = getAlignment(left, right);
            alignments.add(alignment);
        }
        return alignments;
    }

    private static List<CellSource> split(CharSequence input, int lineNumber, int inputFirstColumn) {
        int firstIndex = input.charAt(0) == '|' ? 1 : 0;
        int firstColumn = inputFirstColumn + firstIndex;

        List<CellSource> cells = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean escape = false;
        for (int i = firstIndex; i < input.length(); i++) {
            char c = input.charAt(i);
            if (escape) {
                escape = false;
                sb.append(c);
            } else {
                switch (c) {
                    case '\\':
                        escape = true;
                        // Removing the escaping '\' is handled by the inline parser later, so add it to cell
                        sb.append(c);
                        break;
                    case '|':
                        String content = sb.toString();
                        cells.add(CellSource.of(content, lineNumber, firstColumn));
                        sb.setLength(0);
                        // + 1 to skip the pipe itself for the next cell's span
                        firstColumn = inputFirstColumn + i + 1;
                        break;
                    default:
                        sb.append(c);
                }
            }
        }
        if (sb.length() > 0) {
            String content = sb.toString();
            cells.add(CellSource.of(content, lineNumber, firstColumn));
        }
        return cells;
    }

    private static TableCell.Alignment getAlignment(boolean left, boolean right) {
        if (left && right) {
            return TableCell.Alignment.CENTER;
        } else if (left) {
            return TableCell.Alignment.LEFT;
        } else if (right) {
            return TableCell.Alignment.RIGHT;
        } else {
            return null;
        }
    }

    public static class Factory extends AbstractBlockParserFactory {

        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            CharSequence line = state.getLine();
            CharSequence paragraph = matchedBlockParser.getParagraphContent();
            if (paragraph != null && paragraph.toString().contains("|") && !paragraph.toString().contains("\n")) {
                CharSequence separatorLine = line.subSequence(state.getIndex(), line.length());
                if (TABLE_HEADER_SEPARATOR.matcher(separatorLine).matches()) {
                    List<CellSource> headParts = split(paragraph, 0, 0);
                    List<CellSource> separatorParts = split(separatorLine, 0, 0);
                    if (separatorParts.size() >= headParts.size()) {
                        TableBlockParser parser = new TableBlockParser(paragraph);
                        List<SourceSpan> sourceSpans = matchedBlockParser.getMatchedBlockParser().getSourceSpans();
                        for (SourceSpan sourceSpan : sourceSpans) {
                            parser.addSourceSpan(sourceSpan);
                        }
                        return BlockStart.of(parser).atIndex(state.getIndex()).replaceActiveBlockParser();
                    }
                }
            }
            return BlockStart.none();
        }
    }

    private static class CellSource {

        private final String content;
        private final SourceSpan sourceSpan;

        public static CellSource of(String content, int lineNumber, int firstColumn) {
            if (!content.isEmpty()) {
                int lastColumn = firstColumn + content.length() - 1;
                return new CellSource(content, SourceSpan.of(lineNumber, firstColumn, lastColumn));
            } else {
                return new CellSource(content, null);
            }
        }

        private CellSource(String content, SourceSpan sourceSpan) {
            this.content = content;
            this.sourceSpan = sourceSpan;
        }
    }
}
