package dev.xdark.jpreprocessor.intellij;

import com.intellij.lexer.Lexer;
import com.intellij.lexer.LexerPosition;
import com.intellij.psi.JavaTokenType;
import com.intellij.psi.tree.IElementType;
import dev.xdark.jpreprocessor.processor.JavaPreprocessor;
import dev.xdark.jpreprocessor.processor.PreprocessorConsumer;
import dev.xdark.jpreprocessor.processor.PreprocessorDirective;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

final class PreprocessorLexer extends Lexer {
    private final Map<Integer, Integer> indexes = new HashMap<>();
    private final Lexer delegate;

    PreprocessorLexer(Lexer lexer) {
        delegate = lexer;
    }

    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
        indexes.clear();
        try {
            JavaPreprocessor.process(IntellijProcessing.newEnvironment(), buffer.subSequence(startOffset, endOffset), new IndicesCollector(indexes));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalStateException ignored) {
        }
        delegate.start(buffer, startOffset, endOffset, initialState);
    }

    @Override
    public int getState() {
        return delegate.getState();
    }

    @Override
    public @Nullable IElementType getTokenType() {
        Lexer delegate = this.delegate;
        int start = delegate.getTokenStart();
        if (indexes.containsKey(start)) {
            return JavaTokenType.C_STYLE_COMMENT;
        }
        IElementType type = delegate.getTokenType();
        // Under the hood, this will call locateToken,
        // which in turn might delegate to flexLocateToken and give
        // us new token start index. Check against it
        start = delegate.getTokenStart();
        if (indexes.containsKey(start)) {
            return JavaTokenType.C_STYLE_COMMENT;
        }
        return type;
    }

    @Override
    public int getTokenStart() {
        return delegate.getTokenStart();
    }

    @Override
    public int getTokenEnd() {
        Lexer delegate = this.delegate;
        Integer end = indexes.get(delegate.getTokenStart());
        if (end != null) {
            return end;
        }
        // See comment above on why we are retrying
        return delegate.getTokenEnd();
    }

    @Override
    public void advance() {
        Lexer delegate = this.delegate;
        Integer end = indexes.get(delegate.getTokenStart());
        if (end != null) {
            delegate.start(delegate.getBufferSequence(), end, delegate.getBufferEnd(), delegate.getState());
        }
        delegate.advance();
    }

    @Override
    public @NotNull LexerPosition getCurrentPosition() {
        return delegate.getCurrentPosition();
    }

    @Override
    public void restore(@NotNull LexerPosition position) {
        delegate.restore(position);
    }

    @Override
    public @NotNull CharSequence getBufferSequence() {
        return delegate.getBufferSequence();
    }

    @Override
    public int getBufferEnd() {
        return delegate.getBufferEnd();
    }

    private static final class IndicesCollector implements PreprocessorConsumer {
        final Map<Integer, Integer> indices;

        IndicesCollector(Map<Integer, Integer> indices) {
            this.indices = indices;
        }

        @Override
        public void definition(int start, int end) {
            indices.put(start, end);
        }

        @Override
        public void directiveCall(int start, int end, PreprocessorDirective result) {
            indices.put(start, end);
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
}
