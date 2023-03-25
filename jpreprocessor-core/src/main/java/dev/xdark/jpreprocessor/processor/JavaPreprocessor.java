package dev.xdark.jpreprocessor.processor;

import dev.xdark.jpreprocessor.parser.*;

import java.io.IOException;

public final class JavaPreprocessor {

    private JavaPreprocessor() {
    }

    public static String process(PreprocessorEnvironment env, CharSequence input) {
        StringBuilder builder = new StringBuilder(input.length());
        try {
            process(env, input, dumper(builder));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return builder.toString();
    }

    public static void process(PreprocessorEnvironment env, CharSequence input, PreprocessorConsumer consumer) throws IOException {
        process(env, StringReader.of(input), consumer);
    }

    public static void process(PreprocessorEnvironment env, StringReader reader, PreprocessorConsumer csm) throws IOException {
        CharSequence text = reader.text();
        Lexer lexer = SourceCodeHelper.newLexer(reader);
        int prepend = 0;
        while (true) {
            Token token = lexer.next();
            int tokenStart = token.start();
            if (prepend != -1 && tokenStart > prepend) {
                csm.append(text, prepend, token.start());
            }
            TokenKind kind = token.kind();
            handle:
            if (kind == JavaTokenKind.SLASHSLASH) {
                reader.skipLine();
                csm.append(text, token.start(), reader.position());
                csm.append('\n');
                prepend = -1;
                continue;
            } else if (kind == JavaTokenKind.IDENTIFIER) {
                Token next = lexer.token(1);
                // TODO see comment below
                if (next.kind() == JavaTokenKind.EXCLAMATION && next.start() == token.end()) {
                    String directiveName = textify(lexer, token);
                    MacroDirective directive = env.getDirective(directiveName);
                    if (directive == null) {
                        break handle;
                        // TODO: maybe all directive should have ()?...
                        // There is a corner case like this one:
                        // if (a != null) - a is a directive call?
                        //throw new IllegalStateException("Unknown directive " + directiveName);
                     }
                    lexer.consumeToken();
                    if (directive.consume(env, lexer, csm)) {
                        int start = next.end();
                        int end = reader.position();
                        csm.directiveCall(start, end, directive(directiveName, output -> {
                            StringReader slice = StringReader.of(text.subSequence(start, end));
                            directive.expand(env, SourceCodeHelper.newLexer(slice), output);
                        }));
                    }
                    prepend =lexer.current().end();
                    continue;
                }
            }
            csm.append(text, token.start(), token.end());
            if (token.kind() == JavaTokenKind.EOF) {
                break;
            }
            prepend = token.end();
        }
    }

    static String textify(Lexer lexer, Token token) {
        return lexer.source().text().subSequence(token.start(), token.end()).toString();
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
            public void directiveCall(int start, int end, PreprocessorDirective result) {
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
