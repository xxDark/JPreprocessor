package dev.xdark.jpreprocessor.processor;

import java.util.HashMap;
import java.util.Map;

public final class PreprocessContext {
    final Map<String, MacroDirective> directives;

    PreprocessContext(Map<String, MacroDirective> directives) {
        this.directives = directives;
    }

    PreprocessContext() {
        this(new HashMap<>());
    }

    public MacroDirective lookup(String name) {
        return directives.get(name);
    }

    public boolean setDirective(String name, MacroDirective directive) {
        if (directive == null) {
            return directives.remove(name) != null;
        } else {
            return directives.put(name, directive) == null;
        }
    }
}
