package dev.xdark.jpreprocessor.processor;

import java.util.HashMap;
import java.util.Map;

final class PreprocessContext {
    final Map<String, MacroDerivative> derivatives;
    final StringBuilder tmp = new StringBuilder();
    boolean child;

    PreprocessContext(Map<String, MacroDerivative> derivatives) {
        this.derivatives = derivatives;
    }

    PreprocessContext() {
        this(new HashMap<>());
    }
}
