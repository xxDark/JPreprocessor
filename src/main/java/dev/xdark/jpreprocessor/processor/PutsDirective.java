package dev.xdark.jpreprocessor.processor;

import dev.xdark.jpreprocessor.parser.JavaTokenKind;
import dev.xdark.jpreprocessor.parser.Lexer;
import dev.xdark.jpreprocessor.parser.Token;
import dev.xdark.jpreprocessor.parser.TokenKind;

import java.io.IOException;

final class PutsDirective implements MacroDirective {

    @Override
    public void expand(PreprocessContext ctx, Lexer lexer, Appendable output) throws IOException {
        lexer.expectNext(JavaTokenKind.LPAREN);
        int depth = 1;
        Token previous = null;
        CharSequence text = lexer.source().text();
        while (true) {
            Token token = lexer.next();
            TokenKind kind = token.kind();
            if (kind == JavaTokenKind.EOF) {
                throw new IllegalStateException("Expected )");
            }
            if (kind == JavaTokenKind.LPAREN) {
                depth++;
            } else if (kind == JavaTokenKind.RPAREN) {
                if (--depth == 0) {
                    break;
                }
            }
            if (previous != null) {
                output.append(text, previous.end(), token.start());
            }
            output.append(text, token.start(), token.end());
            previous = token;
        }
    }

    @Override
    public boolean consume(PreprocessContext ctx, Lexer lexer, PreprocessorConsumer csm) {
        lexer.consumeToken();
        SourceCodeHelper.consumeDirectiveDefinition(lexer);
        return true;
    }
}
