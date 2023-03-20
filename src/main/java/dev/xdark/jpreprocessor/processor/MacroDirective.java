package dev.xdark.jpreprocessor.processor;

import dev.xdark.jpreprocessor.parser.Lexer;

import java.io.IOException;

public interface MacroDirective {

    void expand(PreprocessContext ctx, Lexer lexer, Appendable output) throws IOException;

    boolean consume(PreprocessContext ctx, Lexer lexer, PreprocessorConsumer csm);
}
