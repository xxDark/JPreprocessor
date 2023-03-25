package dev.xdark.jpreprocessor.processor;

import java.util.Map;

public interface PreprocessorEnvironment extends IncludeDiscoverer {

    MacroDirective getDirective(String name);

    boolean setDirective(String name, MacroDirective directive);

    Map<String, MacroDirective> getRegisteredDirectives();

    PreprocessorEnvironment clone();
}
