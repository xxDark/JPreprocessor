package dev.xdark.jpreprocessor.intellij;

import com.intellij.lang.*;
import com.intellij.lang.impl.PsiBuilderFactoryImpl;
import com.intellij.lang.impl.PsiBuilderImpl;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.lang.java.JavaParserDefinition;
import com.intellij.lang.java.lexer.JavaLexer;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PreprocessorPsiBuilderFactory extends PsiBuilderFactoryImpl {

    @Override
    public @NotNull PsiBuilder createBuilder(@NotNull Project project, @NotNull LighterLazyParseableNode chameleon) {
        ParserDefinition parserDefinition = getParserDefinition(null, chameleon.getTokenType());
        return new PsiBuilderImpl(project, parserDefinition, parserDefinition.createLexer(project), chameleon, chameleon.getText());
    }

    @Override
    public @NotNull PsiBuilder createBuilder(@NotNull Project project, @NotNull ASTNode chameleon, @Nullable Lexer lexer, @NotNull Language lang, @NotNull CharSequence seq) {
        return super.createBuilder(project, chameleon, newLexer(lang, project, lexer), lang, seq);
    }

    @Override
    public @NotNull PsiBuilder createBuilder(@NotNull Project project, @NotNull LighterLazyParseableNode chameleon, @Nullable Lexer lexer, @NotNull Language lang, @NotNull CharSequence seq) {
        return super.createBuilder(project, chameleon, newLexer(lang, project, lexer), lang, seq);
    }

    @Override
    public @NotNull PsiBuilder createBuilder(@NotNull ParserDefinition parserDefinition, @NotNull Lexer lexer, @NotNull CharSequence seq) {
        if (parserDefinition instanceof JavaParserDefinition) {
            if (lexer instanceof JavaLexer) {
                lexer = new PreprocessorLexer(lexer);
            }
        }
        return super.createBuilder(parserDefinition, lexer, seq);
    }

    private static @NotNull ParserDefinition getParserDefinition(@Nullable Language language, @NotNull IElementType tokenType) {
        Language adjusted = language == null ? tokenType.getLanguage() : language;
        if (adjusted == JavaLanguage.INSTANCE) {
            return new JavaParserDefinition();
        }
        ParserDefinition parserDefinition = LanguageParserDefinitions.INSTANCE.forLanguage(adjusted);
        if (parserDefinition == null) {
            throw new AssertionError("ParserDefinition absent for language: '" + adjusted.getID() + "' (" + adjusted.getClass().getName() + "), " +
                    "for elementType: '" + tokenType.getDebugName() + "' (" + tokenType.getClass().getName() + ")");
        }
        return parserDefinition;
    }

    private static Lexer newLexer(Language language, Project project, Lexer lexer) {
        if (language == JavaLanguage.INSTANCE && !(lexer instanceof PreprocessorLexer)) {
            if (lexer == null) {
                lexer = PreprocessorParserDefinition.createBaseLexer(project);
            } else if (!(lexer instanceof JavaLexer)) {
                return lexer;
            }
            lexer = new PreprocessorLexer(lexer);
        }
        return lexer;
    }
}
