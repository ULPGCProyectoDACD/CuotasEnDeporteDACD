package org.ulpgc.dacd.control;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

public class WatermarkManager {
    private final Path filePath;

    public WatermarkManager(String filename) {
        this.filePath = Paths.get(filename);
    }

    public Instant getLastProcessedDate() {
        try {
            if (Files.exists(filePath)) {
                String content = Files.readString(filePath).trim();
                if (!content.isEmpty()) {
                    return Instant.parse(content);
                }
            }
        } catch (Exception e) {
            System.err.println("No se pudo leer el archivo de la última fecha. " + e.getMessage());
        }
        return Instant.MIN;
    }

    public void saveLastProcessedDate(Instant date) {
        try {
            Files.writeString(filePath, date.toString());
        } catch (IOException e) {
            System.err.println("Error guardando la última fecha: " + e.getMessage());
        }
    }
}