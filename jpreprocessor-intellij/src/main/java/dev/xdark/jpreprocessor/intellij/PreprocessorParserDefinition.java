package dev.xdark.jpreprocessor.intellij;

import com.intellij.lang.java.JavaParserDefinition;
import com.intellij.lang.java.lexer.JavaLexer;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.LanguageLevelProjectExtension;
import com.intellij.pom.java.LanguageLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PreprocessorParserDefinition extends JavaParserDefinition {

    @Override
    public @NotNull Lexer createLexer(@Nullable Project project) {
        return new PreprocessorLexer(createBaseLexer(project));
    }

    public static Lexer createBaseLexer(@Nullable Project project) {
        LanguageLevel level = project != null ? LanguageLevelProjectExtension.getInstance(project).getLanguageLevel() : LanguageLevel.HIGHEST;
        return new JavaLexer(level);
    }
}
