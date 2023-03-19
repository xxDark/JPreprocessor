package dev.xdark.jpreprocessor.javac;

import com.sun.tools.javac.parser.JavacParser;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.util.Context;
import dev.xdark.jpreprocessor.processor.JavaPreprocessor;

public final class InjectedParserFactory extends ParserFactory {

    public InjectedParserFactory(Context context) {
        super(context);
    }

    @Override
    public JavacParser newParser(CharSequence input, boolean keepDocComments, boolean keepEndPos, boolean keepLineMap) {
        return super.newParser(preprocess(input), keepDocComments, keepEndPos, keepLineMap);
    }

    @Override
    public JavacParser newParser(CharSequence input, boolean keepDocComments, boolean keepEndPos, boolean keepLineMap, boolean parseModuleInfo) {
        return super.newParser(preprocess(input), keepDocComments, keepEndPos, keepLineMap, parseModuleInfo);
    }

    private static CharSequence preprocess(CharSequence cs) {
        return JavaPreprocessor.process(cs.toString());
    }
}
