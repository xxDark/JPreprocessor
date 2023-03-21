package dev.xdark.jpreprocessor.processor;

import dev.xdark.jpreprocessor.parser.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ValueParser {

    private ValueParser() {
    }

    public static Object parseValue(Lexer lexer) {
        Token token = lexer.current();
        lexer.consumeToken();
        TokenKind kind = token.kind();
        if (token instanceof NumericToken) {
            NumericToken nt = (NumericToken) token;
            String text = nt.text();
            int radix = nt.radix();
            if (kind instanceof JavaTokenKind) {
                switch ((JavaTokenKind) kind) {
                    case LONG_VALUE:
                        return Long.parseLong(text, radix);
                    case DOUBLE_VALUE:
                        return Double.parseDouble(text);
                    case INT_VALUE:
                        return Integer.parseInt(text, radix);
                    case FLOAT_VALUE:
                        return Float.parseFloat(text);
                    case CHAR_VALUE:
                        return text.charAt(0);
                }
            }
            throw new IllegalStateException("Unhandled numeric kind: " + kind);
        }
        if (token instanceof TextToken) {
            return '"' + ((TextToken) token).text() + '"';
        }
        if (kind == JavaTokenKind.LBRACKET) {
            return parseArray(lexer);
        }
        if (kind == JavaTokenKind.LBRACE) {
            return parseMap(lexer);
        }
        // At this point, all macros must be expanded before call
        // to this method is made!
        if (kind == JavaTokenKind.IDENTIFIER) {
            // Might be some expression, consume whole lexer here
            return lexer.source().text().toString();
        }
        throw new IllegalStateException("Don't know how to handle " + kind);
    }

    private static Object parseArray(Lexer lexer) {
        List<Object> array = new ArrayList<>();
        while (true) {
            array.add(parseValue(lexer));
            Token token = lexer.current();
            TokenKind kind = token.kind();
            if (kind == JavaTokenKind.RBRACKET) {
                lexer.consumeToken();
                return array;
            }
            if (kind == JavaTokenKind.EOF) {
                throw new IllegalStateException("Unexpected EOF");
            }
            if (kind == JavaTokenKind.COMMA) {
                lexer.consumeToken();
            }
        }
    }

    private static Object parseMap(Lexer lexer) {
        Map<Object, Object> map = new HashMap<>();
        while (true) {
            Object key = parseValue(lexer);
            lexer.expectNext(JavaTokenKind.COLUMN);
            Object value = parseValue(lexer);
            Token token = lexer.current();
            TokenKind kind = token.kind();
            map.put(key, value);
            if (kind == JavaTokenKind.RBRACE) {
                return map;
            }
            if (kind == JavaTokenKind.EOF) {
                throw new IllegalStateException("Unexpected EOF");
            }
            if (kind == JavaTokenKind.COMMA) {
                lexer.consumeToken();
            }
        }
    }
}
