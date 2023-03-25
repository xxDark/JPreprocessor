package dev.xdark.jpreprocessor.processor;

public interface PreprocessorConsumer extends Appendable {

    void definition(int start, int end);

    void macroPrefix(int start, int end, PreprocessorDirective result);

    void macroSuffix(int start, int end, PreprocessorDirective result);
}
