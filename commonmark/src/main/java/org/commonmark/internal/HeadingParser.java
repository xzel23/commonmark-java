package org.commonmark.internal;

import org.commonmark.node.Block;
import org.commonmark.node.Heading;
import org.commonmark.node.SourceSpan;
import org.commonmark.parser.InlineParser;
import org.commonmark.parser.block.*;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HeadingParser extends AbstractBlockParser {

    private static Pattern ATX_HEADING = Pattern.compile("^#{1,6}(?: +|$)");
    private static Pattern ATX_TRAILING = Pattern.compile("(^| ) *#+ *$");
    private static Pattern SETEXT_HEADING = Pattern.compile("^(?:=+|-+) *$");

    private final Heading block = new Heading();
    private final String content;

    public HeadingParser(int level, String content) {
        block.setLevel(level);
        this.content = content;
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public BlockContinue tryContinue(ParserState parserState) {
        // In both ATX and Setext headings, once we have the heading markup, there's nothing more to parse.
        return BlockContinue.none();
    }

    @Override
    public void parseInlines(InlineParser inlineParser) {
        inlineParser.parse(content, block);
    }

    public static class Factory extends AbstractBlockParserFactory {

        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            if (state.getIndent() >= 4) {
                return BlockStart.none();
            }
            CharSequence line = state.getLine();
            int nextNonSpace = state.getNextNonSpaceIndex();
            CharSequence paragraph = matchedBlockParser.getParagraphContent();
            Matcher matcher;
            if ((matcher = ATX_HEADING.matcher(line.subSequence(nextNonSpace, line.length()))).find()) {
                // ATX heading
                int newOffset = nextNonSpace + matcher.group(0).length();
                int level = matcher.group(0).trim().length(); // number of #s
                // remove trailing ###s:
                String content = ATX_TRAILING.matcher(line.subSequence(newOffset, line.length())).replaceAll("");
                HeadingParser parser = new HeadingParser(level, content);
                parser.addSourceSpan(SourceSpans.fromState(state, nextNonSpace));

                return BlockStart.of(parser).atIndex(line.length());

            } else if (paragraph != null &&
                    ((matcher = SETEXT_HEADING.matcher(line.subSequence(nextNonSpace, line.length()))).find())) {
                // setext heading line

                int level = matcher.group(0).charAt(0) == '=' ? 1 : 2;
                String content = paragraph.toString();
                HeadingParser parser = new HeadingParser(level, content);
                List<SourceSpan> sourceSpans = matchedBlockParser.getMatchedBlockParser().getSourceSpans();
                for (SourceSpan sourceSpan : sourceSpans) {
                    parser.addSourceSpan(sourceSpan);
                }
                return BlockStart.of(parser).atIndex(line.length()).replaceActiveBlockParser();
            } else {
                return BlockStart.none();
            }
        }
    }
}
