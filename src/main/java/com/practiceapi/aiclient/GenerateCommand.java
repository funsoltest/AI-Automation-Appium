package com.practiceapi.aiclient;

import java.util.Map;

/**
 * Usage:
 *   mvn exec:java -Dexec.mainClass="com.qaautomation.aiclient.GenerateCommand" -Dexec.args="\"your requirement here\""
 */
public class GenerateCommand {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Requirement dena zaroori hai. Example:");
            System.out.println("mvn exec:java -Dexec.mainClass=\"com.qaautomation.aiclient.GenerateCommand\" -Dexec.args=\"'Login page test banao'\"");
            return;
        }

        String requirement = String.join(" ", args);
        System.out.println("[INFO] Requirement AI ko bheji ja rahi hai...\n");

        String prompt = PromptBuilder.buildGeneratePrompt(requirement);
        String response = AiClient.sendMessage(prompt);

        Map<String, String> files = ResponseParser.parseFiles(response, "GeneratedFile.java");
        String sessionId = SessionStore.createSession(requirement, files);

        System.out.println("===== SESSION: " + sessionId + " =====\n");
        for (Map.Entry<String, String> entry : files.entrySet()) {
            System.out.println("----- FILE: " + entry.getKey() + " -----");
            System.out.println(entry.getValue());
            System.out.println();
        }

        System.out.println("=====================================");
        System.out.println("Review karein aur ek command chunein:");
        System.out.println("  Approve -> mvn exec:java \"-Dexec.mainClass=com.practiceapi.aiclient.ConfirmCommand\" \"-Dexec.args=" + sessionId + "\"");
        System.out.println("  Reject  -> mvn exec:java \"-Dexec.mainClass=com.practiceapi.aiclient.RejectCommand\" \"-Dexec.args=" + sessionId + "\"");
        System.out.println("  Refine  -> mvn exec:java \"-Dexec.mainClass=com.practiceapi.aiclient.RefineCommand\" \"-Dexec.args=" + sessionId + " FileName.java feedback here\"");
    }
}