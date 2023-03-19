package dev.xdark.jpreprocessor.processor;

import java.util.*;

public final class JavaPreprocessor {
    private static final MacroDerivative EMPTY = (ctx, input, output) -> {
    };

    private JavaPreprocessor() {
    }

    public static String process(String input) {
        PreprocessContext ctx = touch(new PreprocessContext());
        processImpl(ctx, new StringReader(input));
        return ctx.output.toString();
    }

    private static void processImpl(PreprocessContext ctx, StringReader reader) {
        StringBuilder output = ctx.output;
        while (reader.canRead()) {
            char c = reader.read();
            if (c == '#') {
                if (!ctx.child && reader.matches("define")) {

                    reader.skip(6);
                    reader.skipWhitespace();
                    String name = reader.readUnquotedString();
                    String eol = reader.skipEOL();
                    if (eol != null) {
                        ctx.derivatives.put(name, EMPTY);
                        output.append(eol);
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
                        ctx.derivatives.put(name, (_ctx, input, builder) -> {
                            String insert = result;
                            List<String> list = args;
                            int len = list.size();
                            if (len != 0) {
                                input.skipWhitespace();
                                List<String> $args = extractArguments(input);
                                if (len != $args.size()) {
                                    throw new IllegalStateException("Mismatched argument length");
                                }
                                StringBuilder tmp = _ctx.tmp;
                                PreprocessContext fork = touch(new PreprocessContext(tmp, ctx.derivatives));
                                fork.child = true;
                                for (int i = 0; i < len; i++) {
                                    tmp.setLength(0);
                                    processImpl(fork, new StringReader($args.get(i)));
                                    $args.set(i, tmp.toString());
                                }
                                tmp.setLength(0);
                                Map<String, MacroDerivative> copy = new HashMap<>(ctx.derivatives);
                                fork = touch(new PreprocessContext(tmp, copy));
                                for (int i = 0; i < len; i++) {
                                    String key = list.get(i);
                                    String s = $args.get(i);
                                    copy.put(key, (__, ____, os) -> {
                                        os.append(s);
                                    });
                                }
                                fork.child = true;
                                processImpl(fork, new StringReader(insert));
                                insert = tmp.toString();
                            }
                            builder.append(insert);
                        });
                    }
                    continue;
                }
                int cursor = reader.getCursor();
                if (cursor > 0) {
                    if (!Character.isWhitespace(reader.peek(-1))) {
                        String macro = reader.readUnquotedString();
                        if (!expandDerivative(ctx, reader, macro, output)) {
                            output.append('#').append(macro);
                        }
                        continue;
                    }
                }
                output.append(c);
            } else if (c == '!') {
                processMacro(ctx, reader, output);
            } else {
                processCode(reader, output, c);
            }
        }
    }

    private static void processMacro(PreprocessContext ctx, StringReader reader, StringBuilder output) {
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
            output.append('!');
            return;
        }
        output.setLength(output.length() - name.length());
        derivative.expand(ctx, reader, output);
    }

    private static void processCode(StringReader reader, StringBuilder output, char c) {
        switch (c) {
            case '/': {
                if (reader.canRead(1)) {
                    c = reader.peek();
                    if (c == '/') {
                        reader.skip();
                        output.append("//");
                        while (reader.canRead()) {
                            c = reader.read();
                            output.append(c);
                            if (c == '\n') break;
                        }
                    } else if (c == '*') {
                        reader.skip();
                        output.append("/*");
                        boolean seenStar = false;
                        while (reader.canRead()) {
                            c = reader.read();
                            output.append(c);
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
                output.append(c);
                boolean skip = false;
                char end = c;
                while (reader.canRead()) {
                    c = reader.read();
                    output.append(c);
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
                output.append(c);
        }
    }

    private static boolean expandDerivative(
            PreprocessContext ctx, StringReader reader,
            String macro, StringBuilder output
    ) {
        MacroDerivative expander = ctx.derivatives.get(macro);
        if (expander == null) {
            return false;
        }
        expander.expand(ctx, reader, output);
        return true;
    }

    static List<String> extractArguments(StringReader input) {
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
}
