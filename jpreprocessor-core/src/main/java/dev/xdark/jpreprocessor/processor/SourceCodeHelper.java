package dev.xdark.jpreprocessor.processor;

import dev.xdark.jpreprocessor.parser.*;

import java.util.ArrayList;
import java.util.List;

public final class SourceCodeHelper {

    private SourceCodeHelper() {
    }

    /**
     * Consumes a range between two tokens.
     * The current token must be of a starting kind.
     * The last token will not be consumed.
     */
    public static void consumeRangeBetween(Lexer lexer, TokenKind a, TokenKind b) {
        int depth = 1;
        while (true) {
            Token token = lexer.next();
            TokenKind kind = token.kind();
            if (kind == JavaTokenKind.EOF) {
                throw new IllegalStateException("Unexpected EOF");
            }
            if (kind == a) {
                depth++;
            } else if (kind == b && --depth == 0) {
                return;
            }
        }
    }

    /**
     * Returns a range between two tokens.
     * The current token must be of a starting kind.
     * The last token will not be consumed.
     */
    public static CodeRange getRangeBetween(Lexer lexer, TokenKind a, TokenKind b) {
        int start = lexer.expect(a).end();
        int depth = 1;
        while (true) {
            Token token = lexer.next();
            TokenKind kind = token.kind();
            if (kind == JavaTokenKind.EOF) {
                throw new IllegalStateException("Unexpected EOF");
            }
            if (kind == a) {
                depth++;
            } else if (kind == b && --depth == 0) {
                return new CodeRange(start, lexer.previous().end());
            }
        }
    }

    public static void consumeDirectiveCall(Lexer lexer) {
        consumeRangeBetween(lexer, JavaTokenKind.LPAREN, JavaTokenKind.RPAREN);
    }

    public static String getDirectiveBody(Lexer lexer) {
        CodeRange range = getRangeBetween(lexer, JavaTokenKind.LBRACE, JavaTokenKind.RBRACE);
        return lexer.source().text()
                .subSequence(range.start(), range.end()).toString();
    }

    public static DirectiveDefinition getDirectiveDefinition(Lexer lexer) {
        List<String> args = getArgumentNames(lexer);
        lexer.expectNext(JavaTokenKind.RPAREN);
        String code = getDirectiveBody(lexer);
        return new DirectiveDefinition(
                args,
                code
        );
    }

    static List<Object> getArgumentValues(Lexer lexer) {
        Token token;
        TokenKind kind;
        List<Object> args = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        CharSequence text = lexer.source().text();
        lexer.expect(JavaTokenKind.LPAREN);
        Token last = null;
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
                if (last != null) {
                    builder.append(text, last.end(), token.start());
                }
                builder.append(text, token.start(), token.end());
                if (kind == JavaTokenKind.LPAREN || kind == JavaTokenKind.LBRACKET || kind == JavaTokenKind.LT) {
                    depth++;
                } else if (kind == JavaTokenKind.RBRACKET || kind == JavaTokenKind.GT) {
                    depth--;
                }
            }
            last = token;
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
            if (kind == JavaTokenKind.EOF) {
                throw new IllegalStateException("Unexpected EOF");
            }
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
