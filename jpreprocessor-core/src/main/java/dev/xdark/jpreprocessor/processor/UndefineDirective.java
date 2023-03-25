package dev.xdark.jpreprocessor.processor;

import dev.xdark.jpreprocessor.parser.JavaTokenKind;
import dev.xdark.jpreprocessor.parser.Lexer;
import dev.xdark.jpreprocessor.parser.Token;

import java.io.IOException;

public class UndefineDirective implements MacroDirective {

    @Override
    public void expand(PreprocessorEnvironment env, Lexer lexer, Appendable output) throws IOException {
        lexer.consumeToken();
        Token identifier = lexer.expectNext(JavaTokenKind.LPAREN);
        lexer.expect(JavaTokenKind.IDENTIFIER);
        lexer.nextExpect(JavaTokenKind.RPAREN);
        String name = JavaPreprocessor.textify(lexer, identifier);
        if (!env.setDirective(name, null)) {
            throw new IllegalStateException("Failed to remove directive name");
        }
    }

    @Override
    public boolean consume(PreprocessorEnvironment env, Lexer lexer, PreprocessorConsumer csm) {
        lexer.nextExpect(JavaTokenKind.LPAREN);
        lexer.nextExpect(JavaTokenKind.IDENTIFIER);
        lexer.nextExpect(JavaTokenKind.RPAREN);
        return true;
    }
}
