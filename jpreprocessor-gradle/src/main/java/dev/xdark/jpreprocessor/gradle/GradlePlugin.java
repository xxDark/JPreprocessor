package dev.xdark.jpreprocessor.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.jvm.toolchain.JavaInstallationMetadata;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.jetbrains.annotations.NotNull;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public final class GradlePlugin implements Plugin<Project> {

    @Override
    public void apply(@NotNull Project project) {
        // TODO
        JPreprocessorExtension extension = project.getExtensions().create("jpreprocessor", JPreprocessorExtension.class);
        TaskContainer tasks = project.getTasks();
        String path = String.format("\"%s\"", getPathToPluginJar());
        for (JavaCompile task : tasks.withType(JavaCompile.class)) {
            task.doFirst("JPreprocessor inject", __ -> {
                JavaInstallationMetadata metadata = task.getJavaCompiler().get().getMetadata();
                JavaLanguageVersion version = metadata.getLanguageVersion();
                String option;
                if (version.compareTo(JavaLanguageVersion.of(8)) > 0) {
                    option = "--processor-path";
                } else {
                    option = "-processorpath";
                }
                List<String> args = task.getOptions().getCompilerArgs();
                args.addAll(
                        Arrays.asList(option, path)
                );
                args.add("-Xplugin:JPreprocessor");
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
