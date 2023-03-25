package dev.xdark.jpreprocessor.parser;

public interface Lexer {

    void consumeToken();

    Token current();

    Token token(int ahead);

    Token previous();

    StringReader source();

    void reset();

    default boolean accept(TokenKind kind) {
        return current().kind() == kind;
    }

    default Token expect(TokenKind kind) {
        Token token = current();
        if (token.kind() != kind) {
            throw new IllegalStateException("Expected " + kind);
        }
        return token;
    }

    default Token next() {
        consumeToken();
        return current();
    }

    default Token nextExpect(TokenKind kind) {
        consumeToken();
        return expect(kind);
    }

    default Token expectNext(TokenKind kind) {
        expect(kind);
        return next();
    }
}
