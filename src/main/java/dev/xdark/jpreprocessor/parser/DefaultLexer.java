package dev.xdark.jpreprocessor.parser;

import java.util.ArrayList;
import java.util.List;

public final class DefaultLexer implements Lexer {

    private final List<Token> cache = new ArrayList<>();
    private final Tokenizer tokenizer;
    private Token previous;
    private Token token;

    public DefaultLexer(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    @Override
    public void consumeToken() {
        previous = token;
        if (!cache.isEmpty()) {
            token = cache.remove(0);
        } else {
            token = tokenizer.readToken();
        }
    }

    @Override
    public Token current() {
        return token(0);
    }

    @Override
    public Token token(int ahead) {
        if (ahead == 0) {
            return token;
        } else {
            lookahead(ahead);
            return cache.get(ahead - 1);
        }
    }

    @Override
    public Token previous() {
        return previous;
    }

    @Override
    public StringReader source() {
        return tokenizer.source();
    }

    @Override
    public void reset() {
        previous = null;
        token = null;
        cache.clear();
        tokenizer.reset();
    }

    private void lookahead(int n) {
        List<Token> cache = this.cache;
        Tokenizer tokenizer = this.tokenizer;
        for (int i = cache.size(); i < n; i++) {
            cache.add(tokenizer.readToken());
        }
    }
}
