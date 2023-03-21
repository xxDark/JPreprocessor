package dev.xdark.jpreprocessor.parser;

import java.util.Map;

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
    COLUMN(":"),
    DOT("."),
    SLASH("/"),
    BACKSLASH("\\"),
    SLASHSLASH("//"),
    SLASHSTAR("/*"),
    STARSLASH("*/"),
    ELLIPSIS("..."),
    EXCLAMATION("!"),
    BACKQUOTE("`"),
    EQ("="),
    PLUS("+"),
    MINUS("-"),
    STAR("*"),
    LT("<"),
    GT(">"),
    INT_VALUE(JavaTokenKindTag.NUMERIC),
    LONG_VALUE(JavaTokenKindTag.NUMERIC),
    FLOAT_VALUE(JavaTokenKindTag.NUMERIC),
    DOUBLE_VALUE(JavaTokenKindTag.NUMERIC),
    CHAR_VALUE(JavaTokenKindTag.NUMERIC),
    TEXT_VALUE(JavaTokenKindTag.STRING);

    private static final Map<String, JavaTokenKind> BY_NAME = TokenKindHelper.collect();
    private final String content;
    private final TokenKindTag tag;

    JavaTokenKind(String content, TokenKindTag tag) {
        this.content = content;
        this.tag = tag;
    }

    JavaTokenKind(String content) {
        this(content, JavaTokenKindTag.DEFAULT);
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
