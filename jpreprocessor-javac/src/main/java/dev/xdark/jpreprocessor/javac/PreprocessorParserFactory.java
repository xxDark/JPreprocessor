package dev.xdark.jpreprocessor.javac;

import com.sun.tools.javac.parser.JavacParser;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.util.Context;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

final class PreprocessorParserFactory extends ParserFactory {
    private final Map<CharSequence, CharSequence> cache = Collections.synchronizedMap(new IdentityHashMap<>());

    public PreprocessorParserFactory(Context context) {
        super(hjackContext(context));
        context.put(parserFactoryKey, this);
    }

    @Override
    public JavacParser newParser(CharSequence input, boolean keepDocComments, boolean keepEndPos, boolean keepLineMap) {
        CharSequence cs = cache.computeIfAbsent(input, JavacProcessing::process);
        try {
            return super.newParser(cs, keepDocComments, keepEndPos, keepLineMap);
        } finally {
            cache.remove(input);
        }
    }

    @Override
    public JavacParser newParser(CharSequence input, boolean keepDocComments, boolean keepEndPos, boolean keepLineMap, boolean parseModuleInfo) {
        CharSequence cs = cache.get(input);
        if (cs == null) {
            cs = JavacProcessing.process(input);
        }
        return super.newParser(cs, keepDocComments, keepEndPos, keepLineMap, parseModuleInfo);
    }

    private static Context hjackContext(Context context) {
        context.put(parserFactoryKey, (ParserFactory) null);
        return context;
    }
}
