package dev.xdark.jpreprocessor.processor;

public interface PreprocessorConsumer extends Appendable {

    void definition(int start, int end);

    void directiveCall(int start, int end, PreprocessorDirective result);
}
