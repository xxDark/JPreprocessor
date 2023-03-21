package dev.xdark.jpreprocessor.processor;

import dev.xdark.jpreprocessor.parser.*;

import java.util.ArrayList;
import java.util.List;

public final class SourceCodeHelper {

    private SourceCodeHelper() {
    }

    public static void consumeDirectiveCall(Lexer lexer) {
        int depth = 1;
        lexer.expect(JavaTokenKind.LPAREN);
        while (true) {
            Token token = lexer.next();
            TokenKind kind = token.kind();
            if (kind == JavaTokenKind.EOF) {
                throw new IllegalStateException("Unexpected EOF");
            }
            if (kind == JavaTokenKind.LPAREN) {
                depth++;
            } else if (kind == JavaTokenKind.RPAREN && --depth == 0) {
                break;
            }
        }
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
            if (kind != JavaTokenKind.RPAREN) {
                if (kind == JavaTokenKind.EOF) {
                    throw new IllegalStateException("Unexpected EOF");
                }
                token = lexer.token(1);
                if (token.kind() == JavaTokenKind.COMMA) {
                    lexer.consumeToken();
                }
                continue;
            }
            break;
        }
        // We need to look ahead here to avoid confusion when silently changing
        // last token in JavaPreprocessor
        token = lexer.token(1);
        kind = token.kind();
        if (kind == JavaTokenKind.LBRACE) {
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
        List<String> args = getArgumentNames(lexer);
        StringReader reader = lexer.source();
        Token token = lexer.next();
        TokenKind kind = token.kind();
        int codeStart = lexer.token(1).start();
        int codeEnd;
        if (kind == JavaTokenKind.LBRACE) {
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
                reader.text().subSequence(codeStart, codeEnd).toString()
        );
    }

    static List<Object> getArgumentValues(Lexer lexer) {
        Token token;
        TokenKind kind;
        List<Object> args = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        CharSequence text = lexer.source().text();
        lexer.expect(JavaTokenKind.LPAREN);
        int depth = 1;
        while (true) {
            token = lexer.next();
            kind = token.kind();
            if (kind == JavaTokenKind.COMMA && depth == 1) {
                addArgument(args, builder);
                builder.setLength(0);
            } else {
                if (kind == JavaTokenKind.RPAREN && --depth == 0) {
                    break;
                }
                builder.append(text, token.start(), token.end());
                if (kind == JavaTokenKind.LPAREN || kind == JavaTokenKind.LBRACKET) {
                    depth++;
                } else if (kind == JavaTokenKind.RBRACKET) {
                    depth--;
                }
            }
        }
        if (builder.length() > 0) {
            addArgument(args, builder);
        }
        return args;
    }

    static List<String> getArgumentNames(Lexer lexer) {
        Token token;
        TokenKind kind;
        List<String> args = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        CharSequence text = lexer.source().text();
        lexer.expect(JavaTokenKind.LPAREN);
        int depth = 1;
        while (true) {
            token = lexer.next();
            kind = token.kind();
            if (kind == JavaTokenKind.COMMA && depth == 1) {
                args.add(builder.toString());
                builder.setLength(0);
            } else {
                if (kind == JavaTokenKind.RPAREN && --depth == 0) {
                    break;
                }
                builder.append(text, token.start(), token.end());
                if (kind == JavaTokenKind.LPAREN) {
                    depth++;
                }
            }
        }
        if (builder.length() > 0) {
            args.add(builder.toString());
        }
        return args;
    }

    static Lexer newLexer(StringReader source) {
        return new DefaultLexer(new DefaultTokenizer(source, new BasicTokens()));
    }

    private static void addArgument(List<Object> args, StringBuilder builder) {
        Lexer argument = newLexer(StringReader.of(builder.toString()));
        argument.consumeToken();
        args.add(ValueParser.parseValue(argument));
    }
}
