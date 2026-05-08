package org.ulpgc.dacd.business;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathResolver {
    public static String resolveEventStorePath() {
        Path baseDir = Paths.get(System.getProperty("user.dir"));

        Path pathFromRoot = baseDir.resolve("eventstore/FootballResult/feeder-results");
        if (Files.exists(pathFromRoot)) return pathFromRoot.toString();

        Path pathFromModule = baseDir.resolveSibling("eventstore").resolve("FootballResult").resolve("feeder-results");
        if (Files.exists(pathFromModule)) return pathFromModule.toString();

        throw new IllegalStateException("❌ ERROR: No se ha podido localizar la carpeta 'eventstore'.");
    }
}