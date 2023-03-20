package dev.xdark.jpreprocessor.processor;

import java.util.List;

public final class DirectiveDefinition {
    private final List<String> arguments;
    private final CodeRange range;

    public DirectiveDefinition(List<String> arguments, CodeRange range) {
        this.arguments = arguments;
        this.range = range;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public CodeRange getRange() {
        return range;
    }
}
