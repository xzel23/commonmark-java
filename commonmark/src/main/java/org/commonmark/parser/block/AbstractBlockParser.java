package org.commonmark.parser.block;

import org.commonmark.node.Block;
import org.commonmark.node.SourceSpan;
import org.commonmark.parser.InlineParser;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractBlockParser implements BlockParser {

    private final List<SourceSpan> sourceSpans = new ArrayList<>();

    @Override
    public boolean isContainer() {
        return false;
    }

    @Override
    public boolean canContain(Block block) {
        return false;
    }

    @Override
    public List<SourceSpan> getSourceSpans() {
        return sourceSpans;
    }

    @Override
    public void addLine(CharSequence line) {
    }

    @Override
    public void onLazyContinuationLine(ParserState state) {
    }

    @Override
    public void closeBlock() {
    }

    @Override
    public void parseInlines(InlineParser inlineParser) {
    }

    protected void addSourceSpan(SourceSpan sourceSpan) {
        sourceSpans.add(sourceSpan);
    }
}
