package org.commonmark.internal;

import org.commonmark.node.Block;
import org.commonmark.node.BlockQuote;
import org.commonmark.parser.block.*;

public class BlockQuoteParser extends AbstractBlockParser {

    private final BlockQuote block = new BlockQuote();

    @Override
    public boolean isContainer() {
        return true;
    }

    @Override
    public boolean canContain(Block block) {
        return true;
    }

    @Override
    public BlockQuote getBlock() {
        return block;
    }

    @Override
    public BlockContinue tryContinue(ParserState state) {
        int nextNonSpace = state.getNextNonSpaceIndex();
        CharSequence line = state.getLine();
        if (state.getIndent() <= 3 && nextNonSpace < line.length() && line.charAt(nextNonSpace) == '>') {
            int newIndex = nextNonSpace + 1;
            if (newIndex < line.length() && line.charAt(newIndex) == ' ') {
                newIndex++;
            }
            addSourceSpan(SourceSpans.fromState(state, nextNonSpace));
            return BlockContinue.atIndex(newIndex);
        } else {
            return BlockContinue.none();
        }
    }

    @Override
    public void onLazyContinuationLine(ParserState state) {
        addSourceSpan(SourceSpans.fromState(state, state.getNextNonSpaceIndex()));
    }

    public static class Factory extends AbstractBlockParserFactory {
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            CharSequence line = state.getLine();
            int nextNonSpace = state.getNextNonSpaceIndex();
            if (state.getIndent() < 4 && line.charAt(nextNonSpace) == '>') {
                int newOffset = nextNonSpace + 1;
                // optional following space
                if (newOffset < line.length() && line.charAt(newOffset) == ' ') {
                    newOffset++;
                }
                BlockQuoteParser parser = new BlockQuoteParser();
                parser.addSourceSpan(SourceSpans.fromState(state, nextNonSpace));
                return BlockStart.of(parser).atIndex(newOffset);
            } else {
                return BlockStart.none();
            }
        }
    }
}
