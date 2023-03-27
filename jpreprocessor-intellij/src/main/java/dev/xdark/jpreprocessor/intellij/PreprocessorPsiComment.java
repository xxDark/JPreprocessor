package dev.xdark.jpreprocessor.intellij;

import com.intellij.psi.impl.source.tree.PsiCommentImpl;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

public final class PreprocessorPsiComment extends PsiCommentImpl {

    public PreprocessorPsiComment(@NotNull IElementType type, @NotNull CharSequence text) {
        super(type, text);
    }
}
