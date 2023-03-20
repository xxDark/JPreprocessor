package dev.xdark.jpreprocessor.processor;

import dev.xdark.jpreprocessor.parser.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class JavaPreprocessor {

    private JavaPreprocessor() {
    }

    public static String process(CharSequence input) {
        StringBuilder builder = new StringBuilder(input.length());
        try {
            process(input, dumper(builder));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return builder.toString();
    }

    public static void process(CharSequence input, PreprocessorConsumer consumer) throws IOException {
        PreprocessContext ctx = new PreprocessContext();
        PreprocessorEnvironment.initBuiltins(ctx);
        processImpl(ctx, StringReader.of(input), consumer);
    }

    private static void processImpl(PreprocessContext ctx, StringReader reader, PreprocessorConsumer csm) throws IOException {
        CharSequence text = reader.text();
        Lexer lexer = newLexer(reader);
        Token last = null;
        loop:
        while (true) {
            Token token = lexer.next();
            int prepend = 0;
            if (last != null) {
                prepend = last.end();
            }
            csm.append(text, prepend, token.start());
            TokenKind kind = token.kind();
            if (kind == JavaTokenKind.SLASHSLASH) {
                reader.skipLine();
                csm.append(text, token.start(), reader.position());
                csm.append('\n');
                last = null;
                continue;
            } else if (kind == JavaTokenKind.SLASHSTAR) {
                while (true) {
                    Token tmp = lexer.next();
                    kind = tmp.kind();
                    if (kind == JavaTokenKind.EOF) {
                        throw new IllegalStateException("Expected */");
                    }
                    if (kind == JavaTokenKind.STARSLASH) {
                        last = tmp;
                        csm.append(text, token.start(), tmp.end());
                        continue loop;
                    }
                }
            } else if (kind == JavaTokenKind.IDENTIFIER) {
                Token next = lexer.token(1);
                if (next.kind() == JavaTokenKind.EXCLAMATION) {
                    String directiveName = textify(lexer, token);
                    MacroDirective directive = ctx.lookup(directiveName);
                    if (directive == null) {
                        throw new IllegalStateException("Unknown directive " + directiveName);
                     }
                    lexer.consumeToken();
                    if (directive.consume(ctx, lexer, csm)) {
                        int start = next.end();
                        int end = reader.position();
                        // TODO FIX ME: context should not be shared, and API should be
                        // changed to store a bit more information
                        csm.macroSuffix(start, end, directive(directiveName, output -> {
                            StringReader slice = StringReader.of(text.subSequence(start, end));
                            directive.expand(ctx, newLexer(slice), output);
                        }));
                    }
                    last = lexer.current();
                    continue;
                }
            }
            csm.append(text, token.start(), token.end());
            if (token.kind() == JavaTokenKind.EOF) {
                break;
            }
            last = token;
        }
    }

    static String textify(Lexer lexer, Token token) {
        return lexer.source().text().subSequence(token.start(), token.end()).toString();
    }

    private static Lexer newLexer(StringReader source) {
        return new DefaultLexer(new DefaultTokenizer(source, new BasicTokens()));
    }

    private static PreprocessorDirective directive(String name, Evaluate evaluate) {
        return new PreprocessorDirective() {
            @Override
            public String directiveName() {
                return name;
            }

            @Override
            public void evaluate(Appendable output) throws IOException {
                evaluate.evaluate(output);
            }
        };
    }

    private static PreprocessorConsumer dumper(StringBuilder sb) {
        return new PreprocessorConsumer() {

            @Override
            public void definition(int start, int end) {
            }

            @Override
            public void macroPrefix(int start, int end, PreprocessorDirective result) {
                try {
                    result.evaluate(sb);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }

            @Override
            public void macroSuffix(int start, int end, PreprocessorDirective result) {
                try {
                    result.evaluate(sb);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }

            @Override
            public Appendable append(CharSequence csq) throws IOException {
                sb.append(csq);
                return this;
            }

            @Override
            public Appendable append(CharSequence csq, int start, int end) throws IOException {
                sb.append(csq, start, end);
                return this;
            }

            @Override
            public Appendable append(char c) throws IOException {
                sb.append(c);
                return this;
            }
        };
    }

    private interface Evaluate {

        void evaluate(Appendable output) throws IOException;
    }
}
