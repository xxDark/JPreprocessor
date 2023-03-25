package dev.xdark.jpreprocessor.parser;

public class Token {

    private final TokenKind kind;
    private final int start, end;

    public Token(TokenKind kind, int start, int end) {
        this.kind = kind;
        this.start = start;
        this.end = end;
    }

    public TokenKind kind() {
        return kind;
    }

    public int start() {
        return start;
    }

    public int end() {
        return end;
    }

    @Override
    public String toString() {
        return "Token{" +
                "kind=" + kind +
                ", start=" + start +
                ", end=" + end +
                '}';
    }
}
