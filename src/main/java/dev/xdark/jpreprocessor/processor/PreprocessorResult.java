package dev.xdark.jpreprocessor.processor;

import java.io.IOException;

public interface PreprocessorResult {

    void evaluate(Appendable output) throws IOException;
}
