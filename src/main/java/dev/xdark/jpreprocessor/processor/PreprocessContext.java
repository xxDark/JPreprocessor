package dev.xdark.jpreprocessor.processor;

import java.util.HashMap;
import java.util.Map;

final class PreprocessContext {
    final StringBuilder output;
    final Map<String, MacroDerivative> derivatives;
    final StringBuilder tmp = new StringBuilder();
    boolean child;

    PreprocessContext(StringBuilder output, Map<String, MacroDerivative> derivatives) {
        this.output = output;
        this.derivatives = derivatives;
    }

    PreprocessContext() {
        this(new StringBuilder(), new HashMap<>());
    }
}
