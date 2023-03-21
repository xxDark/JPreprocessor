package dev.xdark.jpreprocessor.processor;

import dev.xdark.jpreprocessor.parser.JavaTokenKind;
import dev.xdark.jpreprocessor.parser.Lexer;
import dev.xdark.jpreprocessor.parser.Token;
import dev.xdark.jpreprocessor.parser.TokenKind;

import java.io.IOException;

final class DefineDirective implements MacroDirective {

    @Override
    public void expand(PreprocessorEnvironment env, Lexer lexer, Appendable output) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean consume(PreprocessorEnvironment env, Lexer lexer, PreprocessorConsumer csm) {
        int start = lexer.previous().start();
        lexer.consumeToken();
        Token token = lexer.current();
        TokenKind kind = token.kind();
        if (kind != JavaTokenKind.IDENTIFIER) {
            throw new IllegalStateException("Expected identifier");
        }
        String name = JavaPreprocessor.textify(lexer, token);
        lexer.consumeToken();
        DirectiveDefinition definition = SourceCodeHelper.getDirectiveDefinition(lexer);
        csm.definition(start, lexer.source().position());
        env.setDirective(name, new UserDirective(definition));
        return false;
    }
}
