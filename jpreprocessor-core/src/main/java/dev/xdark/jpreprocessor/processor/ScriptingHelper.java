package dev.xdark.jpreprocessor.processor;

import dev.xdark.jpreprocessor.parser.Lexer;
import org.mozilla.javascript.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class ScriptingHelper {
    ScriptingHelper() {
    }

    static void setup(Context context) {
        // Set language level to ES6
        context.setLanguageVersion(Context.VERSION_ES6);
        // Turn off all optimizations, we are not running large chunks of code (probably?)
        context.setOptimizationLevel(5);
        // Disable wrap of primitives into native objects
        WrapFactory factory = context.getWrapFactory();
        factory.setJavaPrimitiveWrap(false);
    }

    static void bind(Scriptable scope, PreprocessorEnvironment env, Lexer lexer, Appendable a) {
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

    static Object toScriptableValue(Context ctx, Scriptable scope, Object value) {
        if (value instanceof Object[]) {
            Object[] array = (Object[]) value;
            for (int i = 0; i < array.length; ) {
                array[i] = toScriptableValue(ctx, scope, array[i++]);
            }
            return ctx.newArray(scope, array);
        }
        if (value instanceof List<?>) {
            return toScriptableValue(ctx, scope, ((List<?>) value).toArray());
        }
        if (value instanceof Map) {
            Scriptable object = ctx.newObject(scope);
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                Object key = entry.getKey();
                Object v = entry.getValue();
                if (key instanceof String) {
                    ScriptableObject.putProperty(object, (String) key, toScriptableValue(ctx, scope, v));
                } else if (key instanceof Integer) {
                    ScriptableObject.putProperty(object, (Integer) key, toScriptableValue(ctx, scope, v));
                } else {
                    throw new IllegalStateException("Don't know how to handle " + key);
                }
            }
        }
        return value;
    }
}
