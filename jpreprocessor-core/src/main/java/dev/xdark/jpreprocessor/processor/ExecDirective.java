package dev.xdark.jpreprocessor.processor;

import dev.xdark.jpreprocessor.parser.JavaTokenKind;
import dev.xdark.jpreprocessor.parser.Lexer;
import dev.xdark.jpreprocessor.parser.StringReader;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

import java.io.IOException;

public final class ExecDirective implements MacroDirective {

    @Override
    public void expand(PreprocessorEnvironment env, Lexer lexer, Appendable output) throws IOException {
        CharSequence code = lexer.source().text();
        code = JavaPreprocessor.process(env, code);
        lexer = SourceCodeHelper.newLexer(StringReader.of(code));
        try (Context context = Context.enter()) {
            // Setup context
            ScriptingHelper.setup(context);
            ScriptableObject scope = context.initStandardObjects();
            // Bind all builtins
            ScriptingHelper.bind(scope, env, lexer, output);
            context.evaluateString(scope, code.toString(), null, 0, null);
        }
    }

    @Override
    public boolean consume(PreprocessorEnvironment env, Lexer lexer, PreprocessorConsumer csm) {
        lexer.consumeToken();
        SourceCodeHelper.consumeRangeBetween(lexer, JavaTokenKind.LBRACE, JavaTokenKind.RBRACE);
        return true;
    }
}
