package dev.xdark.jpreprocessor.processor;

import dev.xdark.jpreprocessor.parser.Lexer;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

final class ScriptingHelper {

    ScriptingHelper() {
    }

    static void setup(Context context) {
        // Turn off all optimizations, we are not running large chunks of code (probably?)
        context.setOptimizationLevel(-1);
        // Disable wrap of primitives into native objects
        context.getWrapFactory().setJavaPrimitiveWrap(false);
    }

    static void bind(ScriptableObject scope, PreprocessorEnvironment env, Lexer lexer, Appendable a) {
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
