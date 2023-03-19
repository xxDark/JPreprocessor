package dev.xdark.jpreprocessor.processor;

import java.util.List;
import java.util.Map;

public class PreprocessorEnvironment {
    private static final String UNDEF = "undef";

    static void initBuiltins(PreprocessContext ctx) {
        Map<String, MacroDerivative> derivatives = ctx.derivatives;
        if (!derivatives.containsKey(UNDEF)) {
            derivatives.put(UNDEF, (context, reader, output) -> {
                List<String> args = JavaPreprocessor.extractArguments(reader);
                if (args.size() != 1) {
                    throw new IllegalStateException("Bad call to undef");
                }
                String name = args.get(0);
                if (context.derivatives.remove(name) == null) {
                    throw new IllegalStateException("Could not remove derivative " + name);
                }
            });
        }
    }
}
