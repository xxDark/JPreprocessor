package dev.xdark.jpreprocessor.processor;

import dev.xdark.jpreprocessor.parser.Lexer;

import java.io.IOException;

final class UserDirective implements MacroDirective {
    private final DirectiveDefinition definition;

    UserDirective(DirectiveDefinition definition) {
        this.definition = definition;
    }

    @Override
    public void expand(PreprocessContext ctx, Lexer lexer, Appendable output) throws IOException {
        lexer.consumeToken();
        SourceCodeHelper.consumeDirectiveDefinition(lexer);
    }

    @Override
    public boolean consume(PreprocessContext ctx, Lexer lexer, PreprocessorConsumer csm) {
        lexer.consumeToken();
        SourceCodeHelper.consumeDirectiveDefinition(lexer);
        return true;
    }
}
