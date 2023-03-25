package dev.xdark.jpreprocessor.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.api.tasks.compile.JavaCompile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class GradlePlugin implements Plugin<Project> {

    @Override
    public void apply(@NotNull Project project) {
        TaskContainer tasks = project.getTasks();
        File pluginFile = getPathToPluginJar().toFile();
        for (JavaCompile task : tasks.withType(JavaCompile.class)) {
            task.doFirst("JPreprocessor inject", __ -> {
                CompileOptions options = task.getOptions();
                FileCollection fc = options.getAnnotationProcessorPath();
                if (fc != null) {
                    options.setAnnotationProcessorPath(fc.plus(project.files(pluginFile)));
                    task.getOptions().getCompilerArgs().add("-Xplugin:JPreprocessor");
                }
            });
        }
    }

    static Path getPathToPluginJar() {
        try {
            return Paths.get(GradlePlugin.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException ex) {
            throw new IllegalStateException("Cannot inject into the compiler: plugin location isn't available", ex);
        }
    }
}
