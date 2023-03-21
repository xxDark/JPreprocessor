package dev.xdark.jpreprocessor.processor;

import dev.xdark.jpreprocessor.parser.Lexer;
import dev.xdark.jpreprocessor.parser.StringReader;
import org.mozilla.javascript.*;

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
            // Setup context
            ScriptingHelper.setup(context);
            ScriptableObject scope = context.initStandardObjects();
            // Bind all builtins
            ScriptingHelper.bind(scope, env, lexer, output);
            List<String> args = def.getArguments();
            if (!args.isEmpty()) {
                lexer.consumeToken();
                List<Object> values = SourceCodeHelper.getArgumentValues(lexer);
                int actualCount = values.size();
                boolean varargs = "...".equals(args.get(args.size() - 1));
                if (varargs) {
                    List<Object> tail = values.subList(args.size() - 1, actualCount);
                    Object[] array = tail.toArray();
                    tail.clear();
                    actualCount = args.size() - 1;
                    ScriptableObject.putProperty(scope, "va_args", context.newArray(scope, array));
                } else if (args.size() != actualCount) {
                    throw new IllegalStateException("Argument count mismatch");
                }
                for (int i = 0; i < actualCount; i++) {
                    Object value = values.get(i);
                    if (value instanceof List<?>) {
                        value = context.newArray(scope, ((List<?>) value).toArray());
                    }
                    ScriptableObject.putProperty(scope, args.get(i), value);
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
}
