package com.practiceapi.aiclient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * sessions/sessions.json mein session data rakhta hai:
 * requirement, files (filename -> code), status (pending/approved/rejected).
 * Ye Spring Boot ke SessionStoreService ki local, bridge-free replacement hai.
 */
public class SessionStore {

    private static final Path SESSIONS_DIR = Paths.get("sessions");
    private static final Path SESSIONS_FILE = SESSIONS_DIR.resolve("sessions.json");
    private static final ObjectMapper mapper = new ObjectMapper();

    private static ObjectNode loadAll() throws IOException {
        File file = SESSIONS_FILE.toFile();
        if (!file.exists()) {
            return mapper.createObjectNode();
        }
        JsonNode node = mapper.readTree(file);
        return node.isObject() ? (ObjectNode) node : mapper.createObjectNode();
    }

    private static void saveAll(ObjectNode sessions) throws IOException {
        SESSIONS_DIR.toFile().mkdirs();
        mapper.writerWithDefaultPrettyPrinter().writeValue(SESSIONS_FILE.toFile(), sessions);
    }

    public static String createSession(String requirement, Map<String, String> files) throws IOException {
        String sessionId = UUID.randomUUID().toString().substring(0, 8);

        ObjectNode sessions = loadAll();
        ObjectNode session = mapper.createObjectNode();
        session.put("requirement", requirement);
        session.put("status", "pending");
        session.put("created_at", LocalDateTime.now().toString());

        ObjectNode filesNode = mapper.createObjectNode();
        for (Map.Entry<String, String> entry : files.entrySet()) {
            filesNode.put(entry.getKey(), entry.getValue());
        }
        session.set("files", filesNode);

        sessions.set(sessionId, session);
        saveAll(sessions);
        return sessionId;
    }

    public static JsonNode getSession(String sessionId) throws IOException {
        ObjectNode sessions = loadAll();
        return sessions.get(sessionId);
    }

    public static void updateFiles(String sessionId, Map<String, String> files) throws IOException {
        ObjectNode sessions = loadAll();
        ObjectNode session = (ObjectNode) sessions.get(sessionId);
        if (session == null) {
            throw new IOException("Session " + sessionId + " nahi mila");
        }
        ObjectNode filesNode = mapper.createObjectNode();
        for (Map.Entry<String, String> entry : files.entrySet()) {
            filesNode.put(entry.getKey(), entry.getValue());
        }
        session.set("files", filesNode);
        saveAll(sessions);
    }

    public static void updateStatus(String sessionId, String status) throws IOException {
        ObjectNode sessions = loadAll();
        ObjectNode session = (ObjectNode) sessions.get(sessionId);
        if (session == null) {
            throw new IOException("Session " + sessionId + " nahi mila");
        }
        session.put("status", status);
        saveAll(sessions);
    }

    public static void printPending() throws IOException {
        ObjectNode sessions = loadAll();
        Iterator<Map.Entry<String, JsonNode>> it = sessions.fields();
        boolean any = false;
        while (it.hasNext()) {
            Map.Entry<String, JsonNode> entry = it.next();
            if ("pending".equals(entry.getValue().path("status").asText())) {
                System.out.println(entry.getKey() + " -> " + entry.getValue().path("requirement").asText());
                any = true;
            }
        }
        if (!any) {
            System.out.println("Koi pending session nahi hai.");
        }
    }
}