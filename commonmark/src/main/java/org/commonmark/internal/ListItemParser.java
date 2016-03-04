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

    private int itemIndent;

    public ListItemParser(int itemIndent, ListBlockParser listBlockParser, SourceSpan sourceSpan) {
        this.itemIndent = itemIndent;
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
    public BlockContinue tryContinue(ParserState state) {
        if (state.isBlank() && block.getFirstChild() != null) {
            return BlockContinue.atIndex(state.getNextNonSpaceIndex());
        }

        if (state.getIndent() >= itemIndent) {
            SourceSpan sourceSpan = SourceSpans.fromState(state, state.getNextNonSpaceIndex());
            listBlockParser.addSourceSpanFromItem(sourceSpan);
            addSourceSpan(sourceSpan);
            return BlockContinue.atColumn(state.getColumn() + itemIndent);
        } else {
            return BlockContinue.none();
        }
    }

    @Override
    public void onLazyContinuationLine(ParserState state) {
        addSourceSpan(SourceSpans.fromState(state, state.getNextNonSpaceIndex()));
    }
}
