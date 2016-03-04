package org.commonmark.test;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Node;
import org.commonmark.node.SourceSpan;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SourceSpanRenderer {

    public static String render(Node document, String source) {
        SourceSpanMarkersVisitor visitor = new SourceSpanMarkersVisitor();
        document.accept(visitor);
        Map<Integer, Map<Integer, List<String>>> markers = visitor.getMarkers();

        StringBuilder sb = new StringBuilder();

        String[] lines = source.split("\n");

        int lineNumber = 1;
        for (String line : lines) {
            Map<Integer, List<String>> lineMarkers = markers.get(lineNumber);
            for (int i = 0; i < line.length(); i++) {
                appendMarkers(lineMarkers, i + 1, sb);
                sb.append(line.charAt(i));
            }
            appendMarkers(lineMarkers, line.length() + 1, sb);
            sb.append("\n");
            lineNumber++;
        }

        return sb.toString();
    }

    private static void appendMarkers(Map<Integer, List<String>> lineMarkers, int columnNumber, StringBuilder sb) {
        if (lineMarkers != null) {
            List<String> columnMarkers = lineMarkers.get(columnNumber);
            if (columnMarkers != null) {
                for (String marker : columnMarkers) {
                    sb.append(marker);
                }
            }
        }
    }

    private static class SourceSpanMarkersVisitor extends AbstractVisitor {

        private final Map<Integer, Map<Integer, List<String>>> markers = new HashMap<>();
        private final String opening = "({[<⸢⸤";
        private final String closing = ")}]>⸣⸥";

        private int markerIndex;

        public Map<Integer, Map<Integer, List<String>>> getMarkers() {
            return markers;
        }

        @Override
        protected void visitChildren(Node parent) {
            if (!parent.getSourceSpans().isEmpty()) {
                for (SourceSpan sourceSpan : parent.getSourceSpans()) {
                    String opener = String.valueOf(opening.charAt(markerIndex % opening.length()));
                    String closer = String.valueOf(closing.charAt(markerIndex % closing.length()));

                    getMarkers(sourceSpan.getLineNumber(), sourceSpan.getFirstColumn()).add(opener);
                    getMarkers(sourceSpan.getLineNumber(), sourceSpan.getLastColumn() + 1).add(0, closer);
                }
                markerIndex++;
            }
            super.visitChildren(parent);
        }

        private List<String> getMarkers(int lineNumber, int columnNumber) {
            Map<Integer, List<String>> columnMap = markers.get(lineNumber);
            if (columnMap == null) {
                columnMap = new HashMap<>();
                markers.put(lineNumber, columnMap);
            }

            List<String> markers = columnMap.get(columnNumber);
            if (markers == null) {
                markers = new LinkedList<>();
                columnMap.put(columnNumber, markers);
            }

            return markers;
        }
    }
}
