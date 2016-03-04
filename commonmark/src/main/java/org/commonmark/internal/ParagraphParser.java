package org.commonmark.internal;

import org.commonmark.internal.util.Parsing;
import org.commonmark.node.Block;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SourceSpan;
import org.commonmark.parser.block.AbstractBlockParser;
import org.commonmark.parser.block.BlockContinue;
import org.commonmark.parser.InlineParser;
import org.commonmark.parser.block.ParserState;

public class ParagraphParser extends AbstractBlockParser {

    private final Paragraph block = new Paragraph();
    private BlockContent content = new BlockContent();

    public ParagraphParser(SourceSpan sourceSpan) {
        addSourceSpan(sourceSpan);
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public BlockContinue tryContinue(ParserState state) {
        if (!state.isBlank()) {
            addSourceSpan(SourceSpans.fromState(state, state.getNextNonSpaceIndex()));
            return BlockContinue.atIndex(state.getIndex());
        } else {
            return BlockContinue.none();
        }
    }

    @Override
    public void addLine(CharSequence line) {
        content.add(line);
    }

    @Override
    public void onLazyContinuationLine(ParserState state) {
        addSourceSpan(SourceSpans.fromState(state, state.getNextNonSpaceIndex()));
    }

    public void closeBlock(InlineParserImpl inlineParser) {
        String contentString = content.getString();
        boolean hasReferenceDefs = false;

        int pos;
        // try parsing the beginning as link reference definitions:
        while (contentString.length() > 3 && contentString.charAt(0) == '[' &&
                (pos = inlineParser.parseReference(contentString)) != 0) {
            contentString = contentString.substring(pos);
            hasReferenceDefs = true;
        }
        if (hasReferenceDefs && Parsing.isBlank(contentString)) {
            block.unlink();
            content = null;
        } else {
            block.setSourceSpans(getSourceSpans());
            content = new BlockContent(contentString);
        }
    }

    @Override
    public void parseInlines(InlineParser inlineParser) {
        if (content != null) {
            inlineParser.parse(content.getString(), block);
        }
    }

    public String getContentString() {
        return content.getString();
    }
}
