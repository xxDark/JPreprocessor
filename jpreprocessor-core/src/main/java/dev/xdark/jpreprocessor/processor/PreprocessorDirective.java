package dev.xdark.jpreprocessor.processor;

import java.io.IOException;

public interface PreprocessorDirective {

    String directiveName();

    void evaluate(Appendable output) throws IOException;
}
