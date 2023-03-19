package dev.xdark.jpreprocessor.intellij;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import dev.xdark.jpreprocessor.processor.JavaPreprocessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.IOException;

public final class PreprocessorAnnotator extends ExternalAnnotator<PsiFile, PreprocessorData> {

    private final TextAttributes directives =
            new TextAttributes(
                    DefaultLanguageHighlighterColors.KEYWORD.getDefaultAttributes().getForegroundColor(),
                    null, null, null, Font.BOLD);

    @Override
    public @NotNull PsiFile collectInformation(@NotNull PsiFile file) {
        return file;
    }

    @Override
    public @Nullable PsiFile collectInformation(@NotNull PsiFile file, @NotNull Editor editor, boolean hasErrors) {
        if (hasErrors) return file;
        return null;
    }

    @Override
    public @Nullable PreprocessorData doAnnotate(PsiFile collectedInfo) {
        return ApplicationManager.getApplication().runReadAction((Computable<PreprocessorData>) () -> {
            PreprocessorData data = new PreprocessorData();
            String tet = collectedInfo.getText();
            try {
                JavaPreprocessor.process(tet, data);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            return data;
        });
    }

    @Override
    public void apply(@NotNull PsiFile file, PreprocessorData annotationResult, @NotNull AnnotationHolder holder) {
        long[] pairs = annotationResult.pairs;
        for (int i = 0, j = annotationResult.length; i < j; i++) {
            long pair = pairs[i];
            int start = (int) pair;
            int end = (int) (pair >>> 32);
            holder.newAnnotation(HighlightSeverity.INFORMATION, "")
                    .enforcedTextAttributes(directives)
                    .range(TextRange.create(start, end))
                    .create();
        }
    }
}
