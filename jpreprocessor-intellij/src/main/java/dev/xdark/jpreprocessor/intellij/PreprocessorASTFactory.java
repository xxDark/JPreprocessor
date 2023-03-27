package dev.xdark.jpreprocessor.intellij;

import com.intellij.psi.JavaTokenType;
import com.intellij.psi.impl.source.tree.JavaASTFactory;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.JavaElementKind;
import org.jetbrains.annotations.NotNull;

public final class PreprocessorASTFactory extends JavaASTFactory {

    @Override
    public LeafElement createLeaf(@NotNull IElementType type, @NotNull CharSequence text) {
        if (type == JavaTokenType.C_STYLE_COMMENT) {
            String s = text.toString();
            if (!s.startsWith("/*") && !s.endsWith("*/")) {
                // TODO this is stupid.
                // Look, I tried to make custom token type, but it just breaks
                // with some weird error, like up-to date mismatch?
                return new PreprocessorPsiComment(type, text);
            }
        }
        return super.createLeaf(type, text);
    }
}
