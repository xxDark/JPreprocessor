package dev.xdark.jpreprocessor.javac;

import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class AccessPatcher {

    public static void patch() {
        try {
            Class.forName("java.lang.Module");
            openPackages();
            patchReflectionFilters();
        } catch (ClassNotFoundException ignored) {
        }
    }

    private static void openPackages() {
        try {
            Class<?> Module = Class.forName("java.lang.Module", true, null);
            Method export = Module.getDeclaredMethod("implAddOpens", String.class);
            MethodType type = MethodType.methodType(Module);
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            Unsafe unsafe = (Unsafe) f.get(null);
            f = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            MethodHandles.publicLookup();
            MethodHandles.Lookup lookup = (MethodHandles.Lookup) unsafe.getObject(unsafe.staticFieldBase(f), unsafe.staticFieldOffset(f));
            MethodHandle CLASS_MODULE = lookup.findVirtual(Class.class, "getModule", type);
            MethodHandle CLASS_LOADER_MODULE = lookup.findVirtual(ClassLoader.class, "getUnnamedModule", type);
            MethodHandle METHOD_MODIFIERS = lookup.findSetter(Method.class, "modifiers", Integer.TYPE);
            METHOD_MODIFIERS.invoke(export, Modifier.PUBLIC);
            Set<Object> modules = new HashSet<>();
            Class<?> classBase = AccessPatcher.class;
            Object base = (Object) CLASS_MODULE.invoke(classBase);
            Class<?> ModuleLayer = Class.forName("java.lang.ModuleLayer");
            Method modulesForLayer = ModuleLayer.getDeclaredMethod("modules");
            modulesForLayer.setAccessible(true);
            Method getLayer = Module.getDeclaredMethod("getLayer");
            getLayer.setAccessible(true);
            Object baseLayer = getLayer.invoke(base);
            if (baseLayer != null)
                modules.addAll((Collection) modulesForLayer.invoke(baseLayer));
            Method boot = ModuleLayer.getDeclaredMethod("boot");
            boot.setAccessible(true);
            modules.addAll((Collection) modulesForLayer.invoke(boot.invoke(null)));
            for (ClassLoader cl = classBase.getClassLoader(); cl != null; cl = cl.getParent()) {
                modules.add(CLASS_LOADER_MODULE.invoke(cl));
            }
            Method getPackages = Module.getDeclaredMethod("getPackages");
            getPackages.setAccessible(true);
            for (Object module : modules) {
                for (String name : (Collection<String>) getPackages.invoke(module)) {
                    try {
                        export.invoke(module, name);
                    } catch (Exception ex) {
                        System.err.println("Could not export package " + name + " in module " + module);
                        ex.printStackTrace();
                    }
                }
            }
        } catch (Throwable t) {
            throw new IllegalStateException("Could not export packages", t);
        }
    }

    static long field;

    private static void patchReflectionFilters() {
        Class<?> klass;
        try {
            klass = Class.forName("jdk.internal.reflect.Reflection", true, null);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Unable to locate 'jdk.internal.reflect.Reflection' class", ex);
        }
        try {
            Field[] fields;
            try {
                Method m = Class.class.getDeclaredMethod("getDeclaredFieldsImpl");
                m.setAccessible(true);
                fields = (Field[]) m.invoke(klass);
            } catch (Throwable t) {
                try {
                    Method m = Class.class.getDeclaredMethod("getDeclaredFields0", Boolean.TYPE);
                    m.setAccessible(true);
                    fields = (Field[]) m.invoke(klass, false);
                } catch (Throwable t1) {
                    t.addSuppressed(t1);
                    throw new RuntimeException("Unable to get all class fields", t);
                }
            }
            int c = 0;
            for (Field field : fields) {
                String name = field.getName();
                if ("fieldFilterMap".equals(name) || "methodFilterMap".equals(name)) {
                    field.setAccessible(true);
                    field.set(null, new HashMap<>(0));
                    if (++c == 2) {
                        return;
                    }
                }
            }
            System.err.println("One of field patches did not apply properly. " +
                    "Expected to patch two fields, but patched: " + c);
        } catch (Throwable t) {
            throw new RuntimeException("Unable to patch reflection filters", t);
        }
    }
}
