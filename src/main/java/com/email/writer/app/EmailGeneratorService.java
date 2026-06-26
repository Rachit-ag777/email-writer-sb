package com.email.writer.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Map;

@Service
public class EmailGeneratorService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${groq.api.url}")
    private String groqApiUrl;

    @Value("${groq.model}")
    private String groqModel;

    public EmailGeneratorService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public String generateEmailReply(EmailRequest emailRequest) {
        String prompt = buildPrompt(emailRequest);
        boolean isGroq = geminiApiKey != null && geminiApiKey.startsWith("gsk_");

        if (isGroq) {
            Map<String, Object> requestBody = Map.of(
                    "model", groqModel,
                    "messages", new Object[]{
                            Map.of("role", "user", "content", prompt)
                    }
            );

            String response = webClient.post()
                    .uri(groqApiUrl)
                    .header("Authorization", "Bearer " + geminiApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return extractGroqResponseContent(response);
        } else {
            Map<String, Object> requestBody = Map.of(
                    "contents", new Object[]{
                            Map.of("parts", new Object[]{
                                    Map.of("text", prompt)
                            })
                    }
            );

            String response = webClient.post()
                    .uri(geminiApiUrl)
                    .header("x-goog-api-key", geminiApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return extractGeminiResponseContent(response);
        }
    }

    private String extractGeminiResponseContent(String response) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            return rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();
        } catch (Exception e) {
            return "Error processing Gemini request: " + e.getMessage();
        }
    }

    private String extractGroqResponseContent(String response) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            return rootNode.path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();
        } catch (Exception e) {
            return "Error processing Groq request: " + e.getMessage();
        }
    }

    private String buildPrompt(EmailRequest emailRequest) {

        String tone = emailRequest.getTone();

        if (tone == null || tone.isBlank()) {
            tone = "professional";
        }

        return """
            You are an expert email assistant.

            Write a %s email reply to the email below.

            STRICT RULES:
            - Return ONLY the email reply.
            - Do NOT explain the email.
            - Do NOT analyze the email.
            - Do NOT provide multiple options.
            - Do NOT provide templates.
            - Do NOT use markdown.
            - Do NOT use bullet points.
            - Do NOT include phrases like:
              "Option 1"
              "Option 2"
              "Here are three templates"
              "You can send"
            - Do NOT repeat the original email.
            - Do NOT quote the original email.
            - Do NOT use > symbols.
            - Keep the response under 150 words.
            - Start with a greeting.
            - End with a professional closing.

            Email:
            %s
            """.formatted(tone, emailRequest.getEmailContent());
    }
}
