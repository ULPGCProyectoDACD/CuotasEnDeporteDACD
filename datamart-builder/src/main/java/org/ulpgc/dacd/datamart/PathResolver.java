package org.ulpgc.dacd.datamart;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathResolver {
    private final String basePathStr;

    public PathResolver(String basePathStr) {
        this.basePathStr = basePathStr;
    }

    public String resolveEventStorePath() {
        Path baseDir = Paths.get(basePathStr);

        Path pathFromRoot = baseDir.resolve("eventstore/FootballResult/feeder-results");
        if (Files.exists(pathFromRoot)) return pathFromRoot.toString();

        Path pathFromModule = baseDir.resolveSibling("eventstore").resolve("FootballResult").resolve("feeder-results");
        if (Files.exists(pathFromModule)) return pathFromModule.toString();

        throw new IllegalStateException("❌ ERROR: No se ha podido localizar la carpeta 'eventstore' usando la ruta base: " + baseDir.toAbsolutePath());
    }
}