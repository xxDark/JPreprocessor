package dev.xdark.jpreprocessor.processor;

public class StringReader {
    private static final char SYNTAX_ESCAPE = '\\';
    private static final char SYNTAX_DOUBLE_QUOTE = '"';
    private static final char SYNTAX_SINGLE_QUOTE = '\'';

    private final String string;
    private int cursor;

    public StringReader(String string) {
        this.string = string;
    }

    public String getString() {
        return string;
    }

    public int getCursor() {
        return cursor;
    }

    public boolean canRead(int length) {
        return cursor + length <= string.length();
    }

    public boolean canRead() {
        return canRead(1);
    }

    public char peek() {
        return string.charAt(cursor);
    }

    public char peek(int offset) {
        return string.charAt(cursor + offset);
    }

    public char read() {
        return string.charAt(cursor++);
    }

    public void skip() {
        cursor++;
    }

    public void skip(int n) {
        cursor += n;
    }

    public String skipEOL() {
        if (!canRead()) return "";
        char c = peek();
        if (c == '\n') {
            skip();
            return "\n";
        }
        if (c == '\r') {
            int cursor = this.cursor++;
            if (canRead() && peek() == '\n') {
                skip();
                return "\r\n";
            } else {
                this.cursor = cursor;
            }
        }
        return null;
    }

    public static boolean isQuotedStringStart(char c) {
        return c == SYNTAX_DOUBLE_QUOTE || c == SYNTAX_SINGLE_QUOTE;
    }

    public void skipWhitespace() {
        while (canRead() && Character.isWhitespace(peek())) {
            skip();
        }
    }

    public static boolean isAllowedInUnquotedString(char c) {
        return c >= '0' && c <= '9'
                || c >= 'A' && c <= 'Z'
                || c >= 'a' && c <= 'z'
                || c == '_' || c == '-'
                || c == '.' || c == '+';
    }

    public boolean matches(String text) {
        return string.regionMatches(cursor, text, 0, text.length());
    }

    public String readUnquotedString() {
        final int start = cursor;
        while (canRead() && isAllowedInUnquotedString(peek())) {
            skip();
        }
        return string.substring(start, cursor);
    }

    public String readStringUntil(char terminator) {
        StringBuilder result = new StringBuilder();
        boolean escaped = false;
        while (canRead()) {
            char c = read();
            if (escaped) {
                if (c == terminator || c == SYNTAX_ESCAPE) {
                    result.append(c);
                    escaped = false;
                } else {
                    cursor--;
                    throw new IllegalStateException("Invalid escape " + c);
                }
            } else if (c == SYNTAX_ESCAPE) {
                escaped = true;
            } else if (c == terminator) {
                return result.toString();
            } else {
                result.append(c);
            }
        }

        throw new IllegalStateException("String falls through");
    }

    public void expect(char c) {
        if (!canRead() || peek() != c) {
            throw new IllegalStateException("expected " + c);
        }
        skip();
    }
}