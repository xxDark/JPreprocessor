package dev.xdark.jpreprocessor.processor;

import dev.xdark.jpreprocessor.parser.JavaTokenKind;
import dev.xdark.jpreprocessor.parser.Lexer;
import dev.xdark.jpreprocessor.parser.Token;
import dev.xdark.jpreprocessor.parser.TokenKind;

import java.io.IOException;

final class PutsDirective implements MacroDirective {

    @Override
    public void expand(PreprocessorEnvironment env, Lexer lexer, Appendable output) throws IOException {
        CharSequence text = lexer.source().text();
        lexer.consumeToken();
        Token previous = lexer.expect(JavaTokenKind.LPAREN);;
        int depth = 1;
        boolean escape = false;
        while (true) {
            Token token = lexer.next();
            int prepend = 0;
            if (previous != null) {
                prepend = previous.end();
            }
            output.append(text, prepend, token.start());
            TokenKind kind = token.kind();
            if (kind == JavaTokenKind.BACKSLASH) {
                escape = true;
                previous = token;
                continue;
            }
            if (escape) {
                escape = false;
            } else {
                if (kind == JavaTokenKind.LPAREN) {
                    depth++;
                } else if (kind == JavaTokenKind.RPAREN && --depth == 0) {
                    break;
                }
            }
            output.append(text, token.start(), token.end());
            previous = token;
        }
    }

    @Override
    public boolean consume(PreprocessorEnvironment env, Lexer lexer, PreprocessorConsumer csm) {
        lexer.consumeToken();
        lexer.expect(JavaTokenKind.LPAREN);
        int depth = 1;
        boolean escape = false;
        while (true) {
            Token token = lexer.next();
            TokenKind kind = token.kind();
            if (kind == JavaTokenKind.EOF) {
                throw new IllegalStateException("Unexpected EOF");
            }
            if (kind == JavaTokenKind.BACKSLASH) {
                escape = true;
                continue;
            }
            if (escape) {
                escape = false;
            } else {
                if (kind == JavaTokenKind.LPAREN) {
                    depth++;
                } else if (kind == JavaTokenKind.RPAREN && --depth == 0) {
                    break;
                }
            }
        }
        return true;
    }
}
