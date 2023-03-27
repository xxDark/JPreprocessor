package dev.xdark.jpreprocessor.intellij;

import com.intellij.openapi.util.TextRange;
import dev.xdark.jpreprocessor.processor.PreprocessorDirective;

final class PreprocessorData {

    final TextRange range;
    final PreprocessorDirective directive;

    PreprocessorData(TextRange range, PreprocessorDirective directive) {
        this.range = range;
        this.directive = directive;
    }
}
