package dev.xdark.jpreprocessor.processor;

public final class CharSequenceReader {
    private static final char SYNTAX_DOUBLE_QUOTE = '"';
    private static final char SYNTAX_SINGLE_QUOTE = '\'';

    private final CharSequence cs;
    private final int limit;
    private int cursor;

    public CharSequenceReader(CharSequence cs, int limit) {
        this.cs = cs;
        this.limit = limit;
    }

    public CharSequenceReader(CharSequence cs) {
        this(cs, cs.length());
    }

    public CharSequenceReader copy(int start, int end) {
        CharSequenceReader reader = new CharSequenceReader(cs, end);
        reader.cursor = start;
        return reader;
    }

    public CharSequence source() {
        return cs;
    }

    public int getCursor() {
        return cursor;
    }

    public void setCursor(int cursor) {
        this.cursor = cursor;
    }

    public boolean canRead(int length) {
        return cursor + length <= limit;
    }

    public boolean canRead() {
        return canRead(1);
    }

    public char peek() {
        return cs.charAt(cursor);
    }

    public char peek(int offset) {
        return cs.charAt(cursor + offset);
    }

    public char read() {
        return cs.charAt(cursor++);
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
        int cursor = this.cursor;
        int left = limit - cursor;
        int length = text.length();
        if (left <= length) {
            return false;
        }
        CharSequence cs = this.cs;
        if (cs instanceof String) {
            return ((String) cs).regionMatches(cursor, text, 0, left);
        }
        int off = 0;
        while (left-- != 0) {
            if (cs.charAt(cursor++) != text.charAt(off++)) {
                return false;
            }
        }
        return true;
    }

    public CharSequence readUnquoted() {
        final int start = cursor;
        while (canRead() && isAllowedInUnquotedString(peek())) {
            skip();
        }
        return cs.subSequence(start, cursor);
    }

    public void expect(char c) {
        if (!canRead() || peek() != c) {
            throw new IllegalStateException("expected " + c);
        }
        skip();
    }
}