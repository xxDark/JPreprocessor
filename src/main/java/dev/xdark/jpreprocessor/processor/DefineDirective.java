package dev.xdark.jpreprocessor.processor;

import dev.xdark.jpreprocessor.parser.JavaTokenKind;
import dev.xdark.jpreprocessor.parser.Lexer;
import dev.xdark.jpreprocessor.parser.Token;
import dev.xdark.jpreprocessor.parser.TokenKind;

import java.io.IOException;

final class DefineDirective implements MacroDirective {

    @Override
    public void expand(PreprocessContext ctx, Lexer lexer, Appendable output) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean consume(PreprocessContext ctx, Lexer lexer, PreprocessorConsumer csm) {
        int start = lexer.previous().start();
        Token token = lexer.expectNext(JavaTokenKind.IDENTIFIER);
        String name = JavaPreprocessor.textify(lexer, token);
        lexer.consumeToken();
        DirectiveDefinition definition = SourceCodeHelper.getDirectiveDefinition(lexer);
        csm.definition(start, lexer.source().position());
        ctx.setDirective(name, new UserDirective(definition));
        return false;
    }
}
