package org.commonmark.internal;

import org.commonmark.node.SourceSpan;
import org.commonmark.parser.block.ParserState;

public class SourceSpans {

    public static SourceSpan fromState(ParserState parserState, int startIndex) {
        return SourceSpan.of(parserState.getLineIndex() + 1, startIndex + 1, parserState.getLine().length());
    }
}
