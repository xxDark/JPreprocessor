package dev.xdark.jpreprocessor.intellij;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.java.IJavaElementType;
import dev.xdark.jpreprocessor.processor.BasicPreprocessorEnvironment;
import dev.xdark.jpreprocessor.processor.IncludeDiscoverer;
import dev.xdark.jpreprocessor.processor.PreprocessorEnvironment;

final class IntellijProcessing {
    static final IElementType ELEMENT_TYPE = new IJavaElementType("PREPROCESSOR_CODE");

    static PreprocessorEnvironment newEnvironment() {
        return new BasicPreprocessorEnvironment(IncludeDiscoverer.noDiscover());
    }
}
