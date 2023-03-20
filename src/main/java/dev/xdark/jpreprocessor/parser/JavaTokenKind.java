package dev.xdark.jpreprocessor.parser;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum JavaTokenKind implements TokenKind {
    EOF(),
    IDENTIFIER(),
    HASHTAG("#"),
    IF("if"),
    ELIF("elif"),
    ENDIF("endif"),
    LPAREN("("),
    RPAREN(")"),
    LBRACE("{"),
    RBRACE("}"),
    LBRACKET("["),
    RBRACKET("]"),
    SEMI(";"),
    COMMA(","),
    DOT("."),
    SLASH("/"),
    SLASHSLASH("//"),
    SLASHSTAR("/*"),
    STARSLASH("*/"),
    ELLIPSIS("..."),
    EXCLAMATION("!"),
    EQ("="),
    PLUS("+"),
    MINUS("-"),
    STAR("*"),
    INT_VALUE(TokenKindTag.NUMERIC),
    LONG_VALUE(TokenKindTag.NUMERIC),
    FLOAT_VALUE(TokenKindTag.NUMERIC),
    DOUBLE_VALUE(TokenKindTag.NUMERIC),
    CHAR_VALU(TokenKindTag.NUMERIC),
    TEXT_VALUE(TokenKindTag.STRING);

    private static final Map<String, JavaTokenKind> BY_NAME = Arrays.stream(values())
            .filter(x -> x.content != null)
            .collect(Collectors.toMap(JavaTokenKind::content, Function.identity()));
    private final String content;
    private final TokenKindTag tag;

    JavaTokenKind(String content, TokenKindTag tag) {
        this.content = content;
        this.tag = tag;
    }

    JavaTokenKind(String content) {
        this(content, TokenKindTag.DEFAULT);
    }

    JavaTokenKind(TokenKindTag tag) {
        this(null, tag);
    }

    JavaTokenKind() {
        this((String) null);
    }

    @Override
    public String content() {
        return content;
    }

    @Override
    public TokenKindTag tag() {
        return tag;
    }

    public static JavaTokenKind lookup(String name) {
        return BY_NAME.get(name);
    }
}
