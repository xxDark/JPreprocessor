package dev.xdark.jpreprocessor.parser;

public class TextToken extends Token {

    private final String text;

    public TextToken(TokenKind kind, int start, int end, String text) {
        super(kind, start, end);
        this.text = text;
    }

    public String text() {
        return text;
    }
}
