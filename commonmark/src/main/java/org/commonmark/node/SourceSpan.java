package org.commonmark.node;

import java.util.Objects;

public class SourceSpan {

    private final int lineNumber;

    // TODO: Confusing with "column" in parser?
    private final int firstColumn;

    // TODO: Use length instead? Not sure.
    private final int lastColumn;

    public static SourceSpan of(int lineNumber, int firstColumn, int lastColumn) {
        return new SourceSpan(lineNumber, firstColumn, lastColumn);
    }

    private SourceSpan(int lineNumber, int firstColumn, int lastColumn) {
        this.lineNumber = lineNumber;
        this.firstColumn = firstColumn;
        this.lastColumn = lastColumn;
    }

    public int getFirstColumn() {
        return firstColumn;
    }

    public int getLastColumn() {
        return lastColumn;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SourceSpan that = (SourceSpan) o;
        return lineNumber == that.lineNumber &&
                firstColumn == that.firstColumn &&
                lastColumn == that.lastColumn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lineNumber, firstColumn, lastColumn);
    }

    @Override
    public String toString() {
        return "SourceSpan{" +
                "lineNumber=" + lineNumber +
                ", firstColumn=" + firstColumn +
                ", lastColumn=" + lastColumn +
                "}";
    }
}
