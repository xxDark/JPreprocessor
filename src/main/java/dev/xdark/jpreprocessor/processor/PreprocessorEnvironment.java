package dev.xdark.jpreprocessor.processor;

public class PreprocessorEnvironment {

    static void initBuiltins(PreprocessContext ctx) {
        ctx.setDirective("define", new DefineDirective());
        ctx.setDirective("undefine", new UndefineDirective());
        ctx.setDirective("puts", new PutsDirective());
    }
}
