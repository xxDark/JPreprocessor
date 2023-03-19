package dev.xdark.jpreprocessor.processor;

import java.io.IOException;
import java.util.*;

@SuppressWarnings({"DuplicatedCode"})
public final class JavaPreprocessor {
    private static final MacroDerivative EMPTY = (ctx, input, output) -> {
    };
    private static final Appendable NULL_APPENDABLE = new Appendable() {
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
    };

    private JavaPreprocessor() {
    }

    public static String process(String input) {
        StringBuilder builder = new StringBuilder(input.length());
        try {
            process(input, dumper(builder));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return builder.toString();
    }

    public static void process(String input, PreprocessorConsumer consumer) throws IOException {
        PreprocessContext ctx = touch(new PreprocessContext());
        processImpl(ctx, new StringReader(input), consumer);
    }

    private static void processImpl(PreprocessContext ctx, StringReader reader, PreprocessorConsumer consumer) throws IOException {
        while (reader.canRead()) {
            char c = reader.read();
            if (c == '#') {
                if (!ctx.child) {
                    if (reader.matches("define")) {
                        int start = reader.getCursor();
                        reader.skip(6);
                        reader.skipWhitespace();
                        String name = reader.readUnquotedString();
                        String eol = reader.skipEOL();
                        if (eol != null) {
                            ctx.derivatives.put(name, EMPTY);
                            consumer.append(eol);
                        } else {
                            List<String> args;
                            if (reader.peek() == '(') {
                                args = extractArguments(reader);
                            } else {
                                if (Character.isWhitespace(reader.peek())) {
                                    reader.skip();
                                }
                                args = Collections.emptyList();
                            }
                            StringBuilder code = ctx.tmp;
                            code.setLength(0);
                            while (reader.canRead()) {
                                c = reader.read();
                                if (c == '\n') {
                                    int offset = -1;
                                    if (reader.peek(offset) == '\r') {
                                        offset = -2;
                                    }
                                    if (reader.peek(offset) == '\\') {
                                        if (offset == -2) {
                                            code.append('\r');
                                        }
                                        code.append('\n');
                                    } else {
                                        reader.skip(offset);
                                        break;
                                    }
                                } else {
                                    code.append(c);
                                }
                            }
                            String result = code.toString();
                            ctx.derivatives.put(name, (_ctx, input, output) -> {
                                CharSequence insert = result;
                                List<String> list = args;
                                int len = list.size();
                                if (len != 0) {
                                    input.skipWhitespace();
                                    List<String> $args = extractArguments(input);
                                    if (len != $args.size()) {
                                        throw new IllegalStateException("Mismatched argument length");
                                    }
                                    StringBuilder tmp = _ctx.tmp;
                                    PreprocessContext fork = touch(new PreprocessContext(_ctx.derivatives));
                                    PreprocessorConsumer dumper = dumper(tmp);
                                    fork.child = true;
                                    for (int i = 0; i < len; i++) {
                                        tmp.setLength(0);
                                        processImpl(fork, new StringReader($args.get(i)), dumper);
                                        $args.set(i, tmp.toString());
                                    }
                                    tmp.setLength(0);
                                    Map<String, MacroDerivative> copy = new HashMap<>(_ctx.derivatives);
                                    fork = touch(new PreprocessContext(copy));
                                    for (int i = 0; i < len; i++) {
                                        String key = list.get(i);
                                        String s = $args.get(i);
                                        copy.put(key, ($ctx, ____, os) -> {
                                            os.append(s);
                                        });
                                    }
                                    fork.child = true;
                                    processImpl(fork, new StringReader((String) insert), dumper(tmp));
                                    insert = tmp;
                                }
                                output.append(insert);
                            });
                        }
                        if (consumer != null) {
                            consumer.definition(start, reader.getCursor());
                        }
                        continue;
                    }
                }
                if (consumer != null) {
                    int cursor = reader.getCursor();
                    if (cursor > 0) {
                        if (!Character.isWhitespace(reader.peek(-1))) {
                            String macro = reader.readUnquotedString();
                            MacroDerivative derivative = ctx.derivatives.get(macro);
                            if (derivative != null) {
                                Map<String, MacroDerivative> copy = new HashMap<>(ctx.derivatives);
                                int end = reader.getCursor();
                                consumer.macroPrefix(cursor, end, output -> {
                                    PreprocessContext _ctx = new PreprocessContext(copy);
                                    StringReader _reader = reader.copy(cursor, end);
                                    derivative.expand(_ctx, _reader, output);
                                });
                            }
                        }
                    }
                }
            } else if (c == '!') {
                processMacro(ctx, reader, consumer);
            } else {
                processCode(reader, consumer, c);
            }
        }
    }

    private static void processMacro(PreprocessContext ctx, StringReader reader, PreprocessorConsumer consumer) throws IOException {
        int cursor = reader.getCursor();
        int offset = -2;
        while (cursor-- > 1) {
            char in = reader.peek(offset);
            if (Character.isWhitespace(in) || in == '.' || in == '(') {
                break;
            }
            offset--;
        }
        cursor = reader.getCursor();
        String name = reader.getString().substring(cursor + offset + 1, cursor - 1);
        MacroDerivative derivative = ctx.derivatives.get(name);
        if (derivative == null) {
            consumer.append('!');
            return;
        }
        Map<String, MacroDerivative> copy = new HashMap<>(ctx.derivatives);
        int $cursor = cursor;
        // Look ahead
        reader.skipWhitespace();
        if (reader.canRead() && reader.peek() == '(') {
            skipArguments(reader);
        }
        int end = reader.getCursor();
        consumer.advance(-name.length());
        consumer.macroSuffix(cursor, end, output -> {
            PreprocessContext _ctx = new PreprocessContext(copy);
            StringReader _reader = reader.copy($cursor, end);
            derivative.expand(_ctx, _reader, output);
        });
    }

    private static void processCode(StringReader reader, Appendable appendable, char c) throws IOException {
        switch (c) {
            case '/': {
                if (reader.canRead(1)) {
                    c = reader.peek();
                    if (c == '/') {
                        reader.skip();
                        appendable.append("//");
                        while (reader.canRead()) {
                            c = reader.read();
                            appendable.append(c);
                            if (c == '\n') break;
                        }
                    } else if (c == '*') {
                        reader.skip();
                        appendable.append("/*");
                        boolean seenStar = false;
                        while (reader.canRead()) {
                            c = reader.read();
                            appendable.append(c);
                            if (c == '*') {
                                seenStar = true;
                            } else {
                                if (c == '/') {
                                    if (seenStar) {
                                        return;
                                    }
                                } else {
                                    seenStar = false;
                                }
                            }
                        }
                    }
                }
                break;
            }
            case '"':
            case '\'': {
                appendable.append(c);
                boolean skip = false;
                char end = c;
                while (reader.canRead()) {
                    c = reader.read();
                    appendable.append(c);
                    if (skip) {
                        skip = false;
                        continue;
                    }
                    if (c == '\\') {
                        skip = true;
                    } else if (c == end) {
                        return;
                    }
                }
                throw new IllegalStateException("Did not end string sequence");
            }
            default:
                appendable.append(c);
        }
    }

    static void skipArguments(StringReader input) throws IOException {
        int callDepth = 0;
        input.expect('(');
        input.skipWhitespace();
        while (true) {
            char c = input.read();
            if (c == '(') {
                callDepth++;
            } else if (c == ')') {
                if (callDepth-- == 0) {
                    break;
                }
            }
            if (c == ',' && callDepth == 0) {
                input.skipWhitespace();
            } else {
                processCode(input, NULL_APPENDABLE, c);
            }
        }
    }

    static List<String> extractArguments(StringReader input) throws IOException {
        List<String> arguments = new ArrayList<>();

        int callDepth = 0;
        StringBuilder argument = new StringBuilder();
        input.expect('(');
        input.skipWhitespace();
        while (true) {
            char c = input.read();
            if (c == '(') {
                callDepth++;
            } else if (c == ')') {
                if (callDepth-- == 0) {
                    break;
                }
            }
            if (c == ',' && callDepth == 0) {
                arguments.add(argument.toString().trim());
                argument.setLength(0);
                input.skipWhitespace();
            } else {
                processCode(input, argument, c);
            }
        }

        if (argument.length() > 0) {
            arguments.add(argument.toString().trim());
        }

        return arguments;
    }

    private static PreprocessContext touch(PreprocessContext ctx) {
        PreprocessorEnvironment.initBuiltins(ctx);
        return ctx;
    }

    private static PreprocessorConsumer dumper(StringBuilder builder) {
        return new PreprocessorConsumer() {
            @Override
            public void advance(int length) {
                builder.setLength(builder.length() + length);
            }

            @Override
            public Appendable append(CharSequence csq) throws IOException {
                builder.append(csq);
                return this;
            }

            @Override
            public Appendable append(CharSequence csq, int start, int end) throws IOException {
                builder.append(csq, start, end);
                return this;
            }

            @Override
            public Appendable append(char c) throws IOException {
                builder.append(c);
                return this;
            }

            @Override
            public void definition(int start, int end) {
            }

            @Override
            public void macroPrefix(int start, int end, PreprocessorResult result) {
                try {
                    result.evaluate(builder);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }

            @Override
            public void macroSuffix(int start, int end, PreprocessorResult result) {
                macroPrefix(start, end, result);
            }
        };
    }
}
