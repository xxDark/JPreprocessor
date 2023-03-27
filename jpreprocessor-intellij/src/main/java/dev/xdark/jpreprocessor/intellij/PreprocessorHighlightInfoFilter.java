package dev.xdark.jpreprocessor.intellij;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoFilterImpl;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public final class PreprocessorHighlightInfoFilter extends HighlightInfoFilterImpl {

    @Override
    public boolean accept(@NotNull HighlightInfo info, PsiFile file) {
        if (file == null) {
            return super.accept(info, null);
        }
        PsiElement firstElem = file.findElementAt(info.getStartOffset());
        if (!(firstElem instanceof PreprocessorPsiComment)) {
            return super.accept(info, file);
        }
        return false;
    }
}
