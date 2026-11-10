package com.practiceapi.aiclient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Aap ke self-hosted AI server (vicuna.cognise.art) ko call karta hai.
 * Config har baar fresh load hoti hai, is liye token update karne ke liye
 * sirf credentials.json edit karna kaafi hai.
 */
public class AiClient {

    public static String sendMessage(String prompt) throws IOException, InterruptedException {
        JsonNode config = Config.load();

        String baseUrl = config.get("base_url").asText();
        String authorization = config.get("authorization").asText();
        String utToken = config.get("ut_token").asText();
        String xAppId = config.get("x_app_id").asText();
        String sessionId = config.get("session_id").asText();
        String mode = config.has("mode") ? config.get("mode").asText() : "auto";
        double temperature = config.has("temperature") ? config.get("temperature").asDouble() : 0.3;
        int maxTokens = config.has("max_tokens") ? config.get("max_tokens").asInt() : 2000;

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode body = mapper.createObjectNode();
        body.put("session_id", sessionId);
        body.put("mode", mode);
        body.put("temperature", temperature);
        body.put("max_tokens", maxTokens);

        ArrayNode messages = mapper.createArrayNode();
        ObjectNode userMessage = mapper.createObjectNode();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.add(userMessage);
        body.set("messages", messages);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .header("Authorization", authorization)
                .header("Content-Type", "application/json")
                .header("UT-Token", utToken)
                .header("X-App-Id", xAppId)
                .timeout(Duration.ofSeconds(90))
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 401 || response.statusCode() == 403) {
            System.out.println("[ERROR] Authentication fail ho gayi (" + response.statusCode() + ").");
            System.out.println("Authorization / UT-Token / session_id expire ho chuke hain (15 min limit).");
            System.out.println("config/credentials.json mein naye values paste karein.");
            System.exit(1);
        }

        if (response.statusCode() != 200) {
            System.out.println("[ERROR] Server ne status " + response.statusCode() + " diya:");
            System.out.println(response.body());
            System.exit(1);
        }

        return extractContent(response.body(), mapper);
    }

    /**
     * Response ka exact JSON field abhi confirm nahi hua, is liye common
     * possibilities try karte hain. Match na ho to raw JSON dikha dete hain
     * taake exact field identify karke isay update kiya ja sake.
     */
    private static String extractContent(String rawJson, ObjectMapper mapper) throws IOException {
        JsonNode root = mapper.readTree(rawJson);
        JsonNode candidate;

        candidate = root.path("choices").path(0).path("message").path("content");
        if (candidate.isTextual()) return candidate.asText();

        candidate = root.path("choices").path(0).path("text");
        if (candidate.isTextual()) return candidate.asText();

        candidate = root.path("message").path("content");
        if (candidate.isTextual()) return candidate.asText();

        candidate = root.path("response");
        if (candidate.isTextual()) return candidate.asText();

        candidate = root.path("data").path("content");
        if (candidate.isTextual()) return candidate.asText();

        candidate = root.path("content");
        if (candidate.isTextual()) return candidate.asText();

        return "[RAW_RESPONSE - field structure confirm karni hai]\n" + root.toPrettyString();
    }
}