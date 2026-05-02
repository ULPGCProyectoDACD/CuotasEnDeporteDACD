package org.ulpgc.dacd.control;

import com.google.gson.JsonParser;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class EventStoreWriter {

    private static final String BASE_DIR = "eventstore";

    public void save(String topicName, String json) throws IOException {
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        String ts = obj.get("ts").getAsString();
        String ss = obj.get("ss").getAsString();

        String day = Instant.parse(ts)
                .atZone(ZoneOffset.UTC)
                .toLocalDate()
                .format(DateTimeFormatter.BASIC_ISO_DATE);

        Path dir = Path.of(BASE_DIR, topicName, ss);
        Files.createDirectories(dir);
        Path file = dir.resolve(day + ".events");

        Files.writeString(file, json + System.lineSeparator(),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);

        System.out.println("[ESB] Evento guardado | Topic: " + topicName
                + " | Fuente: " + ss + " | Fecha: " + day + " | Archivo: " + file);
    }
}