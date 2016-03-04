package org.commonmark.internal;

import org.commonmark.node.Block;
import org.commonmark.node.ListItem;
import org.commonmark.node.SourceSpan;
import org.commonmark.parser.block.AbstractBlockParser;
import org.commonmark.parser.block.BlockContinue;
import org.commonmark.parser.block.ParserState;

public class ListItemParser extends AbstractBlockParser {

    private final ListItem block = new ListItem();
    private final ListBlockParser listBlockParser;

    /**
     * Minimum number of columns that the content has to be indented (relative to the containing block) to be part of
     * this list item.
     */
    private int contentIndent;

    public ListItemParser(int contentIndent, ListBlockParser listBlockParser, SourceSpan sourceSpan) {
        this.contentIndent = contentIndent;
        this.listBlockParser = listBlockParser;
        addSourceSpan(sourceSpan);
    }

    @Override
    public boolean isContainer() {
        return true;
    }

    @Override
    public boolean canContain(Block block) {
        return true;
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public void onLazyContinuationLine(ParserState state) {
        addSourceSpan(SourceSpans.fromState(state, state.getNextNonSpaceIndex()));
    }

    @Override
    public BlockContinue tryContinue(ParserState state) {
        if (state.isBlank()) {
            if (block.getFirstChild() == null) {
                // Blank line after empty list item
                return BlockContinue.none();
            } else {
                return BlockContinue.atIndex(state.getNextNonSpaceIndex());
            }
        }

        if (state.getIndent() >= contentIndent) {
            SourceSpan sourceSpan = SourceSpans.fromState(state, state.getNextNonSpaceIndex());
            listBlockParser.addSourceSpanFromItem(sourceSpan);
            addSourceSpan(sourceSpan);
            return BlockContinue.atColumn(state.getColumn() + contentIndent);
        } else {
            return BlockContinue.none();
        }
    }
}
