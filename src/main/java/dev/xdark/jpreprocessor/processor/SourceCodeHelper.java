package dev.xdark.jpreprocessor.processor;

import dev.xdark.jpreprocessor.parser.*;

import java.util.ArrayList;
import java.util.List;

public final class SourceCodeHelper {

    private SourceCodeHelper() {
    }

    public static void consumeDirectiveDefinition(Lexer lexer) {
        Token token = lexer.current();
        TokenKind kind = token.kind();
        if (kind != JavaTokenKind.LPAREN) {
            throw new IllegalStateException("Expected (");
        }
        while (true) {
            token = lexer.next();
            kind = token.kind();
            if (kind == JavaTokenKind.IDENTIFIER) {
                token = lexer.token(1);
                if (token.kind() == JavaTokenKind.COMMA) {
                    lexer.consumeToken();
                }
                continue;
            }
            if (kind == JavaTokenKind.RPAREN) {
                break;
            }
            throw new IllegalStateException("Expected )");
        }
        // We need to look ahead here to avoid confusion when silently changing
        // last token in JavaPreprocessor
        token = lexer.token(1);
        kind = token.kind();
        if (kind == JavaTokenKind.EQ) {
            lexer.source().skipLine();
        } else if (kind == JavaTokenKind.LBRACE) {
            lexer.consumeToken();
            int depth = 1;
            while (true) {
                token = lexer.next();
                kind = token.kind();
                if (kind == JavaTokenKind.EOF) {
                    throw new IllegalStateException("Unexpected EOF");
                }
                if (kind == JavaTokenKind.LBRACE) {
                    depth++;
                } else if (kind == JavaTokenKind.RBRACE) {
                    if (--depth == 0) {
                        break;
                    }
                }
            }
        }
    }

    public static DirectiveDefinition getDirectiveDefinition(Lexer lexer) {
        Token token = lexer.current();
        TokenKind kind = token.kind();
        if (kind != JavaTokenKind.LPAREN) {
            throw new IllegalStateException("Expected (");
        }
        List<String> args = new ArrayList<>();
        while (true) {
            token = lexer.next();
            kind = token.kind();
            if (kind == JavaTokenKind.IDENTIFIER) {
                String argName = JavaPreprocessor.textify(lexer, token);
                token = lexer.token(1);
                if (token.kind() == JavaTokenKind.COMMA) {
                    lexer.consumeToken();
                }
                args.add(argName);
                continue;
            }
            if (kind == JavaTokenKind.RPAREN) {
                break;
            }
            throw new IllegalStateException("Expected )");
        }
        StringReader reader = lexer.source();
        token = lexer.next();
        kind = token.kind();
        int codeStart = lexer.token(1).start();
        int codeEnd = -1;
        if (kind == JavaTokenKind.EQ) {
            reader.skipLine();
            codeEnd = reader.position();
        } else if (kind == JavaTokenKind.LBRACE) {
            int depth = 1;
            while (true) {
                lexer.consumeToken();
                token = lexer.current();
                kind = token.kind();
                if (kind == JavaTokenKind.EOF) {
                    throw new IllegalStateException("Unexpected EOF");
                }
                if (kind == JavaTokenKind.LBRACE) {
                    depth++;
                } else if (kind == JavaTokenKind.RBRACE) {
                    if (--depth == 0) {
                        codeEnd = lexer.previous().end();
                        break;
                    }
                }
            }
        } else {
            throw new IllegalStateException("Unexpected token " + kind);
        }
        return new DirectiveDefinition(
                args,
                new CodeRange(codeStart, codeEnd)
        );
    }
}
