package dev.xdark.jpreprocessor.processor;

public final class CodeRange {

    private final int start, end;

    public CodeRange(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int start() {
        return start;
    }

    public int end() {
        return end;
    }
}
