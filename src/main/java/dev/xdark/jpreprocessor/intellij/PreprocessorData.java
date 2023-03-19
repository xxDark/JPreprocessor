package dev.xdark.jpreprocessor.intellij;

import dev.xdark.jpreprocessor.processor.PreprocessorConsumer;
import dev.xdark.jpreprocessor.processor.PreprocessorResult;

import java.io.IOException;
import java.util.Arrays;

final class PreprocessorData implements PreprocessorConsumer {
    long[] pairs = new long[16];
    int length;

    @Override
    public void advance(int length) {
    }

    @Override
    public void definition(int start, int end) {
        push(start, end);
    }

    @Override
    public void macroPrefix(int start, int end, PreprocessorResult result) {
        push(start, end);
    }

    @Override
    public void macroSuffix(int start, int end, PreprocessorResult result) {
        push(start, end);
    }

    @Override
    public Appendable append(CharSequence csq) throws IOException {
        return this;
    }

    @Override
    public Appendable append(CharSequence csq, int start, int end) throws IOException {
        return this;
    }

    @Override
    public Appendable append(char c) throws IOException {
        return this;
    }

    private void push(int start, int end) {
        int index = length++;
        long[] pairs = this.pairs;
        if (index == pairs.length) {
            pairs = Arrays.copyOf(pairs, index + 16);
            this.pairs = pairs;
        }
        pairs[index] = start | (long) end << 32;
    }
}
