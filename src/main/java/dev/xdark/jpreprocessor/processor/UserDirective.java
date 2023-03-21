package dev.xdark.jpreprocessor.processor;

import dev.xdark.jpreprocessor.parser.Lexer;
import dev.xdark.jpreprocessor.parser.StringReader;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class UserDirective implements MacroDirective {
    private final DirectiveDefinition definition;

    UserDirective(DirectiveDefinition definition) {
        this.definition = definition;
    }

    @Override
    public void expand(PreprocessorEnvironment env, Lexer lexer, Appendable output) throws IOException {
        DirectiveDefinition def = definition;
        String code = def.getCode();
        try (Context context = Context.enter()) {
            // Process all directives inside this definition
            code = JavaPreprocessor.process(env, code);
            // Process all directives inside the function call
            // We will need new lexer afterwards
            String proceed = JavaPreprocessor.process(env, lexer.source().text());
            lexer = SourceCodeHelper.newLexer(StringReader.of(proceed));
            // Turn off all optimizations, we are not running large chunks of code (probably?)
            context.setOptimizationLevel(-1);
            context.getWrapFactory().setJavaPrimitiveWrap(false);
            ScriptableObject scope = context.initStandardObjects();
            // Bind all builtins
            bind(scope, env, lexer, output);
            List<String> args = def.getArguments();
            if (!args.isEmpty()) {
                lexer.consumeToken();
                List<Object> values = SourceCodeHelper.getArgumentValues(lexer);
                int actualCount = values.size();
                boolean varargs = "...".equals(args.get(args.size() - 1));
                if (varargs) {
                    List<Object> tail = values.subList(args.size() - 1, actualCount);
                    List<Object> compact = new ArrayList<>(tail);
                    tail.clear();
                    actualCount = args.size() - 1;
                    ScriptableObject.putProperty(scope, "va_args", compact);
                } else if (args.size() != actualCount) {
                    throw new IllegalStateException("Argument count mismatch");
                }
                for (int i = 0; i < actualCount; i++) {
                    ScriptableObject.putProperty(scope, args.get(i), values.get(i));
                }
            }
            context.evaluateString(scope, code, null, 0, null);
        }
    }

    @Override
    public boolean consume(PreprocessorEnvironment env, Lexer lexer, PreprocessorConsumer csm) {
        lexer.consumeToken();
        SourceCodeHelper.consumeDirectiveCall(lexer);
        return true;
    }

    private static void bind(ScriptableObject scope, PreprocessorEnvironment env, Lexer lexer, Appendable a) {
        ScriptableObject.putProperty(scope, "env", env);
        ScriptableObject.putProperty(scope, "write", (Callable) (cx, scope1, thisObj, args) -> {
            try {
                a.append(Objects.toString(args[0]));
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            return null;
        });
    }
}
