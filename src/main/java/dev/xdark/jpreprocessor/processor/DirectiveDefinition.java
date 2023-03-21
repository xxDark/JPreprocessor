package dev.xdark.jpreprocessor.processor;

import java.util.List;

public final class DirectiveDefinition {
    private final List<String> arguments;
    private final String code;

    public DirectiveDefinition(List<String> arguments, String range) {
        this.arguments = arguments;
        this.code = range;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public String getCode() {
        return code;
    }
}
