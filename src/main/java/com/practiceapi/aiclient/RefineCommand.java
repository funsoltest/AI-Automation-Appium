package com.practiceapi.aiclient;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Usage:
 *   mvn exec:java -Dexec.mainClass="com.qaautomation.aiclient.RefineCommand" -Dexec.args="<sessionId> <FileName.java> <feedback text>"
 *
 * Example:
 *   mvn exec:java -Dexec.mainClass="com.qaautomation.aiclient.RefineCommand" -Dexec.args="a1b2c3d4 LoginPage.java 'explicit wait add karo'"
 */
public class RefineCommand {

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.out.println("Usage: <sessionId> <FileName.java> <feedback text>");
            return;
        }

        String sessionId = args[0];
        String filename = args[1];
        String feedback = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));

        JsonNode session = SessionStore.getSession(sessionId);
        if (session == null) {
            System.out.println("[ERROR] Session " + sessionId + " nahi mila.");
            return;
        }

        JsonNode filesNode = session.path("files");
        JsonNode existingCodeNode = filesNode.get(filename);
        if (existingCodeNode == null) {
            System.out.println("[ERROR] File '" + filename + "' is session mein nahi mili.");
            System.out.println("Available files: " + filesNode.fieldNames());
            return;
        }

        String previousCode = existingCodeNode.asText();
        String prompt = PromptBuilder.buildRefinePrompt(filename, previousCode, feedback);

        System.out.println("[INFO] Refine request bheji ja rahi hai...\n");
        String response = AiClient.sendMessage(prompt);

        Map<String, String> parsed = ResponseParser.parseFiles(response, filename);
        String newCode = parsed.containsKey(filename) ? parsed.get(filename) : parsed.values().iterator().next();

        // Session ki files map update karo (sirf is file ko replace karo)
        Map<String, String> allFiles = new LinkedHashMap<>();
        filesNode.fields().forEachRemaining(e -> allFiles.put(e.getKey(), e.getValue().asText()));
        allFiles.put(filename, newCode);

        SessionStore.updateFiles(sessionId, allFiles);
        SessionStore.updateStatus(sessionId, "pending"); // dobara review ke liye pending

        System.out.println("----- UPDATED FILE: " + filename + " -----");
        System.out.println(newCode);
        System.out.println("\n[INFO] Session " + sessionId + " update ho gayi, phir se review karein (confirm/reject/refine).");
    }
}