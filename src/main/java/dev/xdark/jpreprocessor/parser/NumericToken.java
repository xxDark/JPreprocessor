package dev.xdark.jpreprocessor.parser;

public final class NumericToken extends TextToken {

    private final int radix;

    public NumericToken(TokenKind kind, int start, int end, String text, int radix) {
        super(kind, start, end, text);
        this.radix = radix;
    }

    public int radix() {
        return radix;
    }
}
