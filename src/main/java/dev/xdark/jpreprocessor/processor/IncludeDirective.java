package dev.xdark.jpreprocessor.processor;

import dev.xdark.jpreprocessor.parser.JavaTokenKind;
import dev.xdark.jpreprocessor.parser.Lexer;
import dev.xdark.jpreprocessor.parser.TextToken;
import dev.xdark.jpreprocessor.parser.Token;

import java.io.IOException;
import java.io.Reader;

final class IncludeDirective implements MacroDirective {

    @Override
    public void expand(PreprocessorEnvironment env, Lexer lexer, Appendable output) throws IOException {
        lexer.consumeToken();
        Token identifier = lexer.expectNext(JavaTokenKind.TEXT_VALUE);
        lexer.expect(JavaTokenKind.IDENTIFIER);
        lexer.nextExpect(JavaTokenKind.RPAREN);
        String path = ((TextToken) identifier).text();
        Reader reader = env.findInclude(path);
        if (reader == null) {
            throw new IllegalStateException("Could not find include file " + path);
        }
        char[] buf = new char[512];
        CharArraySequence cs = new CharArraySequence(buf, 0);
        try {
            int r;
            while ((r = reader.read(buf)) != -1) {
                cs.len = r;
                output.append(cs);
            }
        } finally {
            reader.close();
        }
    }

    @Override
    public boolean consume(PreprocessorEnvironment env, Lexer lexer, PreprocessorConsumer csm) {
        lexer.nextExpect(JavaTokenKind.LPAREN);
        lexer.nextExpect(JavaTokenKind.TEXT_VALUE);
        lexer.nextExpect(JavaTokenKind.RPAREN);
        return true;
    }

    private static final class CharArraySequence implements CharSequence {
        final char[] buf;
        final int offset;
        int len;

        CharArraySequence(char[] buf, int offset) {
            this.buf = buf;
            this.offset = offset;
        }

        @Override
        public int length() {
            return len;
        }

        @Override
        public char charAt(int index) {
            return buf[index];
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            CharArraySequence slice = new CharArraySequence(buf, start);
            slice.len = end - start;
            return slice;
        }
    }
}
