package com.practiceapi.aiclient;

/**
 * Usage:
 *   mvn exec:java -Dexec.mainClass="com.qaautomation.aiclient.RejectCommand" -Dexec.args="<sessionId>"
 */
public class RejectCommand {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Session ID dena zaroori hai.");
            return;
        }
        String sessionId = args[0];

        if (SessionStore.getSession(sessionId) == null) {
            System.out.println("[ERROR] Session " + sessionId + " nahi mila.");
            return;
        }

        SessionStore.updateStatus(sessionId, "rejected");
        System.out.println("[DONE] Session " + sessionId + " reject kar diya gaya. Koi file save nahi hui.");
    }
}