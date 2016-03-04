package org.commonmark.test;

import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.junit.Test;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.*;

public class SourceSpansTest {

    private static final Parser PARSER = Parser.builder().build();

    @Test
    public void paragraph() {
        assertSpans("foo\n", Paragraph.class, SourceSpan.of(1, 1, 3));
        assertSpans("foo\nbar\n", Paragraph.class, SourceSpan.of(1, 1, 3), SourceSpan.of(2, 1, 3));
        assertSpans("  foo\n  bar\n", Paragraph.class, SourceSpan.of(1, 3, 5), SourceSpan.of(2, 3, 5));
        assertSpans("> foo\n> bar\n", Paragraph.class, SourceSpan.of(1, 3, 5), SourceSpan.of(2, 3, 5));
        assertSpans("* foo\n  bar\n", Paragraph.class, SourceSpan.of(1, 3, 5), SourceSpan.of(2, 3, 5));
        assertSpans("* foo\nbar\n", Paragraph.class, SourceSpan.of(1, 3, 5), SourceSpan.of(2, 1, 3));
    }

    @Test
    public void thematicBreak() {
        assertSpans("---\n", ThematicBreak.class, SourceSpan.of(1, 1, 3));
        assertSpans("  ---\n", ThematicBreak.class, SourceSpan.of(1, 3, 5));
        assertSpans("> ---\n", ThematicBreak.class, SourceSpan.of(1, 3, 5));
    }

    @Test
    public void atxHeading() {
        assertSpans("# foo", Heading.class, SourceSpan.of(1, 1, 5));
        assertSpans(" # foo", Heading.class, SourceSpan.of(1, 2, 6));
        assertSpans("## foo ##", Heading.class, SourceSpan.of(1, 1, 9));
        assertSpans("> # foo", Heading.class, SourceSpan.of(1, 3, 7));
    }

    @Test
    public void setextHeading() {
        assertSpans("foo\n===\n", Heading.class, SourceSpan.of(1, 1, 3), SourceSpan.of(2, 1, 3));
        assertSpans("foo\nbar\n====\n", Heading.class, SourceSpan.of(1, 1, 3), SourceSpan.of(2, 1, 3), SourceSpan.of(3, 1, 4));
        assertSpans("  foo\n  ===\n", Heading.class, SourceSpan.of(1, 3, 5), SourceSpan.of(2, 3, 5));
        assertSpans("> foo\n> ===\n", Heading.class, SourceSpan.of(1, 3, 5), SourceSpan.of(2, 3, 5));
    }

    @Test
    public void indentedCodeBlock() {
        assertSpans("    foo\n", IndentedCodeBlock.class, SourceSpan.of(1, 5, 7));
        assertSpans("     foo\n", IndentedCodeBlock.class, SourceSpan.of(1, 5, 8));
        assertSpans("\tfoo\n", IndentedCodeBlock.class, SourceSpan.of(1, 2, 4));
        assertSpans(" \tfoo\n", IndentedCodeBlock.class, SourceSpan.of(1, 3, 5));
        assertSpans("  \tfoo\n", IndentedCodeBlock.class, SourceSpan.of(1, 4, 6));
        assertSpans("   \tfoo\n", IndentedCodeBlock.class, SourceSpan.of(1, 5, 7));
        assertSpans("    \tfoo\n", IndentedCodeBlock.class, SourceSpan.of(1, 5, 8));
        assertSpans("    \t foo\n", IndentedCodeBlock.class, SourceSpan.of(1, 5, 9));
        assertSpans("\t foo\n", IndentedCodeBlock.class, SourceSpan.of(1, 2, 5));
        assertSpans("\t  foo\n", IndentedCodeBlock.class, SourceSpan.of(1, 2, 6));
        assertSpans("    foo\n     bar\n", IndentedCodeBlock.class, SourceSpan.of(1, 5, 7), SourceSpan.of(2, 5, 8));
        assertSpans("    foo\n\tbar\n", IndentedCodeBlock.class, SourceSpan.of(1, 5, 7), SourceSpan.of(2, 2, 4));
        assertSpans("    foo\n    \n     \n", IndentedCodeBlock.class, SourceSpan.of(1, 5, 7), SourceSpan.of(3, 5, 5));
        assertSpans(">     foo\n", IndentedCodeBlock.class, SourceSpan.of(1, 7, 9));
    }

    @Test
    public void fencedCodeBlock() {
        assertSpans("```\nfoo\n```\n", FencedCodeBlock.class,
                SourceSpan.of(1, 1, 3), SourceSpan.of(2, 1, 3), SourceSpan.of(3, 1, 3));
        assertSpans("```\nfoo\nbar\n```\n", FencedCodeBlock.class,
                SourceSpan.of(1, 1, 3), SourceSpan.of(2, 1, 3), SourceSpan.of(3, 1, 3), SourceSpan.of(4, 1, 3));
        assertSpans("```\nfoo\nbar\n```\n", FencedCodeBlock.class,
                SourceSpan.of(1, 1, 3), SourceSpan.of(2, 1, 3), SourceSpan.of(3, 1, 3), SourceSpan.of(4, 1, 3));
        assertSpans("   ```\n   foo\n   ```\n", FencedCodeBlock.class,
                SourceSpan.of(1, 4, 6), SourceSpan.of(2, 4, 6), SourceSpan.of(3, 4, 6));
        assertSpans(" ```\n foo\nfoo\n```\n", FencedCodeBlock.class,
                SourceSpan.of(1, 2, 4), SourceSpan.of(2, 2, 4), SourceSpan.of(3, 1, 3), SourceSpan.of(4, 1, 3));
        assertSpans("```info\nfoo\n```\n", FencedCodeBlock.class,
                SourceSpan.of(1, 1, 7), SourceSpan.of(2, 1, 3), SourceSpan.of(3, 1, 3));
        assertSpans("* ```\n  foo\n  ```\n", FencedCodeBlock.class,
                SourceSpan.of(1, 3, 5), SourceSpan.of(2, 3, 5), SourceSpan.of(3, 3, 5));
        assertSpans("> ```\n> foo\n> ```\n", FencedCodeBlock.class,
                SourceSpan.of(1, 3, 5), SourceSpan.of(2, 3, 5), SourceSpan.of(3, 3, 5));
    }

    @Test
    public void htmlBlock() {
        assertSpans("<div>\n", HtmlBlock.class, SourceSpan.of(1, 1, 5));
        assertSpans(" <div>\n foo\n </div>\n", HtmlBlock.class, SourceSpan.of(1, 1, 6), SourceSpan.of(2, 1, 4), SourceSpan.of(3, 1, 7));
        assertSpans("* <div>\n", HtmlBlock.class, SourceSpan.of(1, 3, 7));
    }

    @Test
    public void blockQuote() {
        assertSpans(">foo\n", BlockQuote.class, SourceSpan.of(1, 1, 4));
        assertSpans("> foo\n", BlockQuote.class, SourceSpan.of(1, 1, 5));
        assertSpans(">  foo\n", BlockQuote.class, SourceSpan.of(1, 1, 6));
        assertSpans(" > foo\n", BlockQuote.class, SourceSpan.of(1, 2, 6));
        assertSpans("   > foo\n  > bar\n", BlockQuote.class, SourceSpan.of(1, 4, 8), SourceSpan.of(2, 3, 7));
        // Lazy continuations
        assertSpans("> foo\nbar\n", BlockQuote.class, SourceSpan.of(1, 1, 5), SourceSpan.of(2, 1, 3));
        assertSpans("> foo\nbar\n> baz\n", BlockQuote.class, SourceSpan.of(1, 1, 5), SourceSpan.of(2, 1, 3), SourceSpan.of(3, 1, 5));
        assertSpans("> > foo\nbar\n", BlockQuote.class, SourceSpan.of(1, 1, 7), SourceSpan.of(2, 1, 3));
    }

    @Test
    public void listBlock() {
        assertSpans("* foo\n", ListBlock.class, SourceSpan.of(1, 1, 5));
        assertSpans("* foo\n  bar\n", ListBlock.class, SourceSpan.of(1, 1, 5), SourceSpan.of(2, 3, 5));
        assertSpans("* foo\n* bar\n", ListBlock.class, SourceSpan.of(1, 1, 5), SourceSpan.of(2, 1, 5));
        assertSpans("* foo\n  # bar\n", ListBlock.class, SourceSpan.of(1, 1, 5), SourceSpan.of(2, 3, 7));
        assertSpans("* foo\n  * bar\n", ListBlock.class, SourceSpan.of(1, 1, 5), SourceSpan.of(2, 3, 7));
        assertSpans("* foo\n> bar\n", ListBlock.class, SourceSpan.of(1, 1, 5));
        assertSpans("> * foo\n", ListBlock.class, SourceSpan.of(1, 3, 7));

        // Lazy continuations
        assertSpans("* foo\nbar\nbaz", ListBlock.class, SourceSpan.of(1, 1, 5), SourceSpan.of(2, 1, 3), SourceSpan.of(3, 1, 3));
        assertSpans("* foo\nbar\n* baz", ListBlock.class, SourceSpan.of(1, 1, 5), SourceSpan.of(2, 1, 3), SourceSpan.of(3, 1, 5));
        assertSpans("* foo\n  * bar\nbaz", ListBlock.class, SourceSpan.of(1, 1, 5), SourceSpan.of(2, 3, 7), SourceSpan.of(3, 1, 3));

        Node document = PARSER.parse("* foo\n  * bar\n");
        ListBlock listBlock = (ListBlock) document.getFirstChild().getFirstChild().getLastChild();
        assertThat(listBlock.getSourceSpans(), contains(SourceSpan.of(2, 3, 7)));
    }

    @Test
    public void listItem() {
        assertSpans("* foo\n", ListItem.class, SourceSpan.of(1, 1, 5));
        assertSpans(" * foo\n", ListItem.class, SourceSpan.of(1, 2, 6));
        assertSpans("  * foo\n", ListItem.class, SourceSpan.of(1, 3, 7));
        assertSpans("   * foo\n", ListItem.class, SourceSpan.of(1, 4, 8));
        assertSpans("*\n  foo\n", ListItem.class, SourceSpan.of(1, 1, 1), SourceSpan.of(2, 3, 5));
        assertSpans("*\n  foo\n  bar\n", ListItem.class, SourceSpan.of(1, 1, 1), SourceSpan.of(2, 3, 5), SourceSpan.of(3, 3, 5));
        assertSpans("> * foo\n", ListItem.class, SourceSpan.of(1, 3, 7));

        // Lazy continuations
        assertSpans("* foo\nbar\n", ListItem.class, SourceSpan.of(1, 1, 5), SourceSpan.of(2, 1, 3));
        assertSpans("* foo\nbar\nbaz\n", ListItem.class, SourceSpan.of(1, 1, 5), SourceSpan.of(2, 1, 3), SourceSpan.of(3, 1, 3));
    }

    @Test
    public void visualCheck() {
        assertEquals("(> {[* <foo>]})\n(>   {[<bar>]})\n(> {⸢* ⸤baz⸥⸣})\n",
                visualizeSourceSpans("> * foo\n>   bar\n> * baz\n"));
        assertEquals("(> {[* <```>]})\n(>   {[<foo>]})\n(>   {[<```>]})\n",
                visualizeSourceSpans("> * ```\n>   foo\n>   ```"));
    }

    private String visualizeSourceSpans(String source) {
        Node document = PARSER.parse(source);
        return SourceSpanRenderer.render(document, source);
    }

    private static void assertSpans(String input, Class<? extends Node> nodeClass, SourceSpan... expectedSourceSpans) {
        Node node = PARSER.parse(input);
        while (node != null && !nodeClass.isInstance(node)) {
            node = node.getFirstChild();
        }
        if (node == null) {
            fail("Expected to find " + nodeClass + " node");
        } else {
            assertThat(node.getSourceSpans(), contains(expectedSourceSpans));
        }
    }
}
