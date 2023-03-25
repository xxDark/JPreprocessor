package dev.xdark.jpreprocessor.gradle;

import org.gradle.api.file.FileCollection;

public abstract class JPreprocessorExtension {

    public abstract FileCollection includeDirectories();
}
