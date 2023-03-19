package dev.xdark.jpreprocessor.javac;

import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.util.Context;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

public final class JavaComplerPlugin implements Plugin, TaskListener {
    private BasicJavacTask task;
    private Unsafe unsafe;
    private long factoryOffset;

    @Override
    public String getName() {
        return "JPreprocessor";
    }

    @Override
    public void init(JavacTask task, String... args) {
        AccessPatcher.patch();
        task.addTaskListener(this);
        this.task = (BasicJavacTask) task;
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe) field.get(null);
            factoryOffset = unsafe.objectFieldOffset(JavaCompiler.class.getDeclaredField("parserFactory"));
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void started(TaskEvent e) {
        if (e.getKind() == TaskEvent.Kind.PARSE) {
            Context ctx = task.getContext();
            JavaCompiler compiler = JavaCompiler.instance(ctx);
            Unsafe u = unsafe;
            long offset = factoryOffset;
            ParserFactory factory = (ParserFactory) u.getObject(compiler, offset);
            if (!(factory instanceof InjectedParserFactory)) {
                factory = new InjectedParserFactory(ctx);
                u.putObject(compiler, offset, factory);
            }
        }
    }
}
