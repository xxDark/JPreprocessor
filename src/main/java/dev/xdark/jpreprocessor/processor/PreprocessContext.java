package dev.xdark.jpreprocessor.processor;

import java.util.HashMap;
import java.util.Map;

final class PreprocessContext {
    final Map<String, MacroDirective> derivatives;
    final StringBuilder tmp = new StringBuilder();
    boolean child;

    PreprocessContext(Map<String, MacroDirective> derivatives) {
        this.derivatives = derivatives;
    }

    PreprocessContext() {
        this(new HashMap<>());
    }
}
