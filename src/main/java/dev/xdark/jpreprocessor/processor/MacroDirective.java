package dev.xdark.jpreprocessor.processor;

import java.io.IOException;

public interface MacroDirective {

    void expand(PreprocessContext ctx, CharSequenceReader reader, Appendable output) throws IOException;
}
