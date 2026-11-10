package com.practiceapi.aiclient;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.File;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.Map;

/**
 * Usage:
 *   mvn exec:java -Dexec.mainClass="com.practiceapi.aiclient.ConfirmCommand" -Dexec.args="<sessionId>"
 *
 * Session ki files ko SEEDHA Maven source folders mein save karta hai
 * (generated_output/ ke bajaye) taake IntelliJ/Maven unhe turant
 * run-able source ke tor par pehchane:
 *   *Page.java -> src/test/java/com/practiceapi/pages/
 *   *Test.java -> src/test/java/com/practiceapi/tests/
 *   baaki      -> src/test/java/com/practiceapi/
 */
public class ConfirmCommand {

    private static final String SRC_TEST_BASE = "src/test/java/com/practiceapi";

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Session ID dena zaroori hai.");
            return;
        }
        String sessionId = args[0];

        JsonNode session = SessionStore.getSession(sessionId);
        if (session == null) {
            System.out.println("[ERROR] Session " + sessionId + " nahi mila.");
            return;
        }

        String status = session.path("status").asText();
        if (!"pending".equals(status)) {
            System.out.println("[WARN] Ye session already '" + status + "' hai, dobara save nahi kiya ja sakta.");
            return;
        }

        JsonNode filesNode = session.path("files");
        Iterator<Map.Entry<String, JsonNode>> it = filesNode.fields();

        while (it.hasNext()) {
            Map.Entry<String, JsonNode> entry = it.next();
            String filename = entry.getKey();
            String code = entry.getValue().asText();

            String subfolder = filename.endsWith("Page.java") ? "pages"
                    : filename.endsWith("Test.java") ? "tests"
                    : "";

            File dir = new File(SRC_TEST_BASE, subfolder);
            dir.mkdirs();
            File outFile = new File(dir, filename);

            try (FileWriter writer = new FileWriter(outFile)) {
                writer.write(code);
            }
            System.out.println("[SAVED] " + outFile.getPath());
        }

        SessionStore.updateStatus(sessionId, "approved");
        System.out.println("\n[DONE] Session " + sessionId + " approved aur saved.");
        System.out.println("IntelliJ mein 'Reload Maven Project' karein taake naye files source tree mein dikhein.");
    }
}