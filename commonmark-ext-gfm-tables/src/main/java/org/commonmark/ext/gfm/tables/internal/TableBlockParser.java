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
            return BlockContinue.atIndex(state.getIndex());
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

            List<String> cells = split(rowLine);

            TableRow tableRow = new TableRow();
            tableRow.setSourceSpans(Collections.singletonList(rowSpan));

            if (headerColumns == -1) {
                headerColumns = cells.size();
            }

            // Body can not have more columns than head
            for (int i = 0; i < headerColumns; i++) {
                String cell = i < cells.size() ? cells.get(i) : "";
                TableCell.Alignment alignment = i < alignments.size() ? alignments.get(i) : null;
                TableCell tableCell = new TableCell();
                tableCell.setHeader(header);
                tableCell.setAlignment(alignment);
                inlineParser.parse(cell.trim(), tableCell);
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
        List<String> parts = split(separatorLine);
        List<TableCell.Alignment> alignments = new ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            boolean left = trimmed.startsWith(":");
            boolean right = trimmed.endsWith(":");
            TableCell.Alignment alignment = getAlignment(left, right);
            alignments.add(alignment);
        }
        return alignments;
    }

    private static List<String> split(CharSequence input) {
        String line = input.toString();//.trim();
        if (line.startsWith("|")) {
            line = line.substring(1);
        }
        List<String> cells = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean escape = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
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
                        cells.add(sb.toString());
                        sb.setLength(0);
                        break;
                    default:
                        sb.append(c);
                }
            }
        }
        if (sb.length() > 0) {
            cells.add(sb.toString());
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
                    List<String> headParts = split(paragraph);
                    List<String> separatorParts = split(separatorLine);
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

}
