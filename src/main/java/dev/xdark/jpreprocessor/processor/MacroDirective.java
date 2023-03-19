package dev.xdark.jpreprocessor.processor;

import java.io.IOException;

public interface MacroDirective {

    void expand(PreprocessContext ctx, StringReader reader, Appendable output) throws IOException;
}
