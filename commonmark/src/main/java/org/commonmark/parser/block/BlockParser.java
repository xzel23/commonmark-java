package org.commonmark.parser.block;

import org.commonmark.node.Block;
import org.commonmark.node.SourceSpan;
import org.commonmark.parser.InlineParser;

import java.util.List;

/**
 * Parser for a specific block node.
 * <p>
 * Implementations should subclass {@link AbstractBlockParser} instead of implementing this directly.
 */
public interface BlockParser {

    /**
     * Return true if the block that is parsed is a container (contains other blocks), or false if it's a leaf.
     */
    boolean isContainer();

    boolean canContain(Block block);

    /**
     * Return the resulting block of this parser. While parsing is still in progress, this might be an unfinished node.
     */
    Block getBlock();

    /**
     * Return the source spans that were computed for this block.
     */
    List<SourceSpan> getSourceSpans();

    BlockContinue tryContinue(ParserState parserState);

    void addLine(CharSequence line);

    /**
     * Called when a lazy continuation line was encountered for any child of this block.
     */
    void onLazyContinuationLine(ParserState parserState);

    void closeBlock();

    void parseInlines(InlineParser inlineParser);

}
