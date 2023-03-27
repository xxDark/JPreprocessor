package dev.xdark.jpreprocessor.intellij;

import com.intellij.openapi.util.TextRange;
import dev.xdark.jpreprocessor.processor.PreprocessorConsumer;
import dev.xdark.jpreprocessor.processor.PreprocessorDirective;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

final class PreprocessorCollector implements PreprocessorConsumer {
    final List<PreprocessorData> data = new ArrayList<>();

    @Override
    public void definition(int start, int end) {
        push(start, end, null);
    }

    @Override
    public void directiveCall(int start, int end, PreprocessorDirective result) {
        push(start, end, result);
    }

    private void push(int start, int end, PreprocessorDirective directive) {
        data.add(new PreprocessorData(TextRange.create(start, end), directive));
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
}
