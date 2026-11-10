package com.practiceapi.aiclient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * config/credentials.json ko HAR CALL par fresh padhta hai (koi caching nahi).
 * Isliye jab bhi Authorization / UT-Token / session_id expire ho (har 15 min),
 * sirf is ek JSON file mein update karein - har command automatically
 * naye values use kar legi.
 */
public class Config {

    // Project root se relative path (jahan se 'mvn exec:java' run karte hain)
    private static final Path CONFIG_PATH = Paths.get("config", "credentials.json");

    public static JsonNode load() throws IOException {
        File file = CONFIG_PATH.toFile();
        if (!file.exists()) {
            throw new IOException("Config file nahi mili: " + file.getAbsolutePath()
                    + " (project root se 'mvn exec:java' run karein)");
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(file);
    }
}