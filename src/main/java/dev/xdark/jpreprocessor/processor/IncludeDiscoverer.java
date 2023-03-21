package dev.xdark.jpreprocessor.processor;

import java.io.Reader;

public interface IncludeDiscoverer {

    Reader findInclude(String fileName);

    static IncludeDiscoverer noDiscover() {
        return fileName -> null;
    }
}
