package dev.xdark.jpreprocessor.parser;

public interface Lexer {

    void consumeToken();

    Token current();

    Token token(int ahead);

    Token previous();

    StringReader source();

    default boolean accept(JavaTokenKind kind) {
        return current().kind() == kind;
    }

    default Token expect(JavaTokenKind kind) {
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

    default Token expectNext(JavaTokenKind kind) {
        consumeToken();
        return expect(kind);
    }
}
