package dev.xdark.jpreprocessor.parser;

public interface Tokenizer {

    Token readToken();

    StringReader source();
}
