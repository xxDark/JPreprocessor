package dev.xdark.jpreprocessor.intellij;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiFile;
import dev.xdark.jpreprocessor.processor.BasicPreprocessorEnvironment;
import dev.xdark.jpreprocessor.processor.IncludeDiscoverer;
import dev.xdark.jpreprocessor.processor.JavaPreprocessor;
import dev.xdark.jpreprocessor.processor.PreprocessorDirective;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

public final class PreprocessorAnnotator extends ExternalAnnotator<PsiFile, List<PreprocessorData>> {

    @Override
    public @Nullable PsiFile collectInformation(@NotNull PsiFile file) {
        return file;
    }

    @Override
    public @Nullable PsiFile collectInformation(@NotNull PsiFile file, @NotNull Editor editor, boolean hasErrors) {
        if (hasErrors) return file;
        return null;
    }

    @Override
    public @Nullable List<PreprocessorData> doAnnotate(PsiFile collectedInfo) {
        return ApplicationManager.getApplication().runReadAction((Computable<List<PreprocessorData>>) () -> {
            PreprocessorCollector collector = new PreprocessorCollector();
            try {
                JavaPreprocessor.process(IntellijProcessing.newEnvironment(), collectedInfo.getText(), collector);
            } catch (Exception ignored) {
            }
            return collector.data;
        });
    }

    @Override
    public void apply(@NotNull PsiFile file, List<PreprocessorData> annotationResult, @NotNull AnnotationHolder holder) {
        for (PreprocessorData data : annotationResult) {
            AnnotationBuilder builder = holder.newSilentAnnotation(HighlightSeverity.INFORMATION);
            builder.range(data.range)
                    .highlightType(ProblemHighlightType.INFORMATION)
                    .enforcedTextAttributes(DefaultLanguageHighlighterColors.TEMPLATE_LANGUAGE_COLOR.getDefaultAttributes())
                    .create();
        }
    }
}
