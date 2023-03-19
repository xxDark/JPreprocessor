package dev.xdark.jpreprocessor.processor;

public interface PreprocessorConsumer extends Appendable {

    void advance(int length);

    void definition(int start, int end);

    void macroPrefix(int start, int end, PreprocessorResult result);

    void macroSuffix(int start, int end, PreprocessorResult result);
}
