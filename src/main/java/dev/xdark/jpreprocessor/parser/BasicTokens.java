package dev.xdark.jpreprocessor.parser;

public final class BasicTokens implements Tokens {

    @Override
    public TokenKind lookup(String content) {
        TokenKind kind = JavaTokenKind.lookup(content);
        if (kind == null) {
            kind = JavaTokenKind.IDENTIFIER;
        }
        return kind;
    }
}
