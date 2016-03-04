package org.commonmark.internal;

import org.commonmark.node.*;
import org.commonmark.parser.block.*;

import java.util.regex.Pattern;

public class IndentedCodeBlockParser extends AbstractBlockParser {

    public static int INDENT = 4;

    private static final Pattern TRAILING_BLANK_LINES = Pattern.compile("(?:\n[ \t]*)+$");

    private final IndentedCodeBlock block = new IndentedCodeBlock();
    private BlockContent content = new BlockContent();

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public BlockContinue tryContinue(ParserState state) {
        if (state.getIndent() >= INDENT) {
            calculateAndAddSourceSpan(state);
            return BlockContinue.atColumn(state.getColumn() + INDENT);
        } else if (state.isBlank()) {
            return BlockContinue.atIndex(state.getNextNonSpaceIndex());
        } else {
            return BlockContinue.none();
        }
    }

    @Override
    public void addLine(CharSequence line) {
        content.add(line);
    }

    @Override
    public void closeBlock() {
        // add trailing newline
        content.add("");
        String contentString = content.getString();
        content = null;

        String literal = TRAILING_BLANK_LINES.matcher(contentString).replaceFirst("\n");
        block.setLiteral(literal);
    }

    private void calculateAndAddSourceSpan(ParserState state) {
        int indentIndex = calculateIndentIndex(state);
        if (indentIndex < state.getLine().length()) {
            addSourceSpan(SourceSpans.fromState(state, indentIndex));
        }
    }

    private static int calculateIndentIndex(ParserState state) {
        int index = state.getIndex();
        while (index < state.getIndex() + INDENT) {
            if (state.getLine().charAt(index) == '\t') {
                return index + 1;
            }
            index++;
        }
        return index;
    }

    public static class Factory extends AbstractBlockParserFactory {

        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            // An indented code block cannot interrupt a paragraph.
            if (state.getIndent() >= INDENT && !state.isBlank() && !(state.getActiveBlockParser().getBlock() instanceof Paragraph)) {
                IndentedCodeBlockParser parser = new IndentedCodeBlockParser();
                parser.calculateAndAddSourceSpan(state);
                return BlockStart.of(parser).atColumn(state.getColumn() + INDENT);
            } else {
                return BlockStart.none();
            }
        }
    }
}

