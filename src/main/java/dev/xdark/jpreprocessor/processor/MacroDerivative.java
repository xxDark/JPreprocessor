package dev.xdark.jpreprocessor.processor;

public interface MacroDerivative {

    void expand(PreprocessContext ctx, StringReader reader, StringBuilder output);
}
