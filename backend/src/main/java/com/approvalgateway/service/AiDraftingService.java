package com.approvalgateway.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Generates the draft action an automation proposes, before a human reviews it.
 *
 * Calls the Anthropic Messages API when ANTHROPIC_API_KEY is set. Falls back to a
 * clearly-labelled mock draft otherwise, so the app is runnable/demoable with zero
 * external configuration (useful for CI, first clone, or a live interview walkthrough).
 */
@Service
public class AiDraftingService {

    private final RestClient restClient = RestClient.create("https://api.anthropic.com");

    @Value("${ai.anthropic.api-key:}")
    private String apiKey;

    @Value("${ai.anthropic.model:claude-3-5-sonnet-20241022}")
    private String model;

    public String draftEmailReply(String customerMessage) {
        if (apiKey == null || apiKey.isBlank()) {
            return "[MOCK — set ANTHROPIC_API_KEY to generate a real draft]\n\n"
                    + "Gentile cliente,\n\ngrazie per averci scritto. Abbiamo ricevuto il suo messaggio:\n\""
                    + customerMessage + "\"\n\nLe risponderemo al più presto.\n\nCordiali saluti";
        }

        String prompt = """
                Sei l'assistente email di un'azienda. Scrivi una bozza di risposta breve,
                professionale e in italiano al seguente messaggio di un cliente. Rispondi
                solo con il testo dell'email, senza commenti aggiuntivi.

                Messaggio del cliente:
                %s
                """.formatted(customerMessage);

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "max_tokens", 1024,
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );

        ClaudeResponse response = restClient.post()
                .uri("/v1/messages")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .header("content-type", "application/json")
                .body(requestBody)
                .retrieve()
                .body(ClaudeResponse.class);

        if (response == null || response.content() == null || response.content().isEmpty()) {
            return "[AI draft unavailable — empty response from Anthropic API]";
        }
        return response.content().get(0).text();
    }

    private record ClaudeResponse(List<ContentBlock> content) {
    }

    private record ContentBlock(String type, String text) {
    }
}
