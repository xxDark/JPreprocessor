package dev.xdark.jpreprocessor.processor;

import dev.xdark.jpreprocessor.parser.Lexer;

import java.io.IOException;

public interface MacroDirective {

    void expand(PreprocessorEnvironment env, Lexer lexer, Appendable output) throws IOException;

    boolean consume(PreprocessorEnvironment env, Lexer lexer, PreprocessorConsumer csm);
}
