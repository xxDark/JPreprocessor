package dev.xdark.jpreprocessor.javac;

import dev.xdark.jpreprocessor.processor.BasicPreprocessorEnvironment;
import dev.xdark.jpreprocessor.processor.IncludeDiscoverer;
import dev.xdark.jpreprocessor.processor.JavaPreprocessor;

final class JavacProcessing {

    private JavacProcessing() {
    }

    static String process(CharSequence cs) {
        return JavaPreprocessor.process(new BasicPreprocessorEnvironment(IncludeDiscoverer.noDiscover()), cs);
    }
}
