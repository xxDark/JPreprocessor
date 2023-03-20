package dev.xdark.jpreprocessor.processor;

import dev.xdark.jpreprocessor.parser.JavaTokenKind;
import dev.xdark.jpreprocessor.parser.Lexer;
import dev.xdark.jpreprocessor.parser.Token;

import java.io.IOException;

public class UndefineDirective implements MacroDirective {

    @Override
    public void expand(PreprocessContext ctx, Lexer lexer, Appendable output) throws IOException {
        lexer.expectNext(JavaTokenKind.LPAREN);
        Token identifier = lexer.expectNext(JavaTokenKind.IDENTIFIER);
        lexer.expectNext(JavaTokenKind.RPAREN);
        String name = JavaPreprocessor.textify(lexer, identifier);
        if (!ctx.setDirective(name, null)) {
            throw new IllegalStateException("Failed to remove directive name");
        }
    }

    @Override
    public boolean consume(PreprocessContext ctx, Lexer lexer, PreprocessorConsumer csm) {
        lexer.expectNext(JavaTokenKind.LPAREN);
        lexer.expectNext(JavaTokenKind.IDENTIFIER);
        lexer.expectNext(JavaTokenKind.RPAREN);
        return true;
    }
}
