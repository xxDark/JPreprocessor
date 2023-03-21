package dev.xdark.jpreprocessor.processor;

import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class BasicPreprocessorEnvironment implements PreprocessorEnvironment {
    private final Map<String, MacroDirective> directives;
    private final IncludeDiscoverer includeDiscoverer;

    public BasicPreprocessorEnvironment(Map<String, MacroDirective> directives, IncludeDiscoverer includeDiscoverer) {
        this.directives = directives;
        this.includeDiscoverer = includeDiscoverer;
    }

    public BasicPreprocessorEnvironment(IncludeDiscoverer includeDiscoverer) {
        this(createDirectives(), includeDiscoverer);
    }

    @Override
    public Reader findInclude(String fileName) {
        return includeDiscoverer.findInclude(fileName);
    }

    @Override
    public MacroDirective getDirective(String name) {
        return directives.get(name);
    }

    @Override
    public boolean setDirective(String name, MacroDirective directive) {
        if (directive == null) {
            return directives.remove(name) != null;
        } else {
            return directives.put(name, directive) == null;
        }
    }

    @Override
    public Map<String, MacroDirective> getRegisteredDirectives() {
        return Collections.unmodifiableMap(directives);
    }

    @Override
    public PreprocessorEnvironment clone() {
        return new BasicPreprocessorEnvironment(new HashMap<>(directives), includeDiscoverer);
    }

    private static Map<String, MacroDirective> createDirectives() {
        Map<String, MacroDirective> map = new HashMap<>();
        map.put("define", new DefineDirective());
        map.put("defined", new DefinedDirective());
        map.put("puts", new PutsDirective());
        map.put("undefine", new UndefineDirective());
        map.put("include", new IncludeDirective());
        map.put("exec", new ExecDirective());
        return map;
    }
}
