package dev.xdark.jpreprocessor.processor;

import java.io.IOException;
import java.io.Reader;

public interface IncludeDiscoverer {

    Reader findInclude(String fileName) throws IOException;

    static IncludeDiscoverer noDiscover() {
        return fileName -> null;
    }
}
