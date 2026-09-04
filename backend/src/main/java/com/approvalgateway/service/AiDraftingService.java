package com.approvalgateway.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Generates the draft action an automation proposes, before a human reviews it.
 *
 * Calls the Google Gemini API (free tier via Google AI Studio) when GEMINI_API_KEY
 * is set. Falls back to a clearly-labelled mock draft otherwise, so the app is
 * runnable/demoable with zero external configuration (useful for CI, first clone,
 * or a live interview walkthrough).
 */
@Service
public class AiDraftingService {

    private final RestClient restClient = RestClient.create("https://generativelanguage.googleapis.com");

    @Value("${ai.gemini.api-key:}")
    private String apiKey;

    @Value("${ai.gemini.model:gemini-3.6-flash}")
    private String model;

    public String draftEmailReply(String customerMessage) {
        if (!hasApiKey()) {
            return "[MOCK — set GEMINI_API_KEY to generate a real draft]\n\n"
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

        return generate(prompt, "[AI draft unavailable — empty response from Gemini API]");
    }

    public String draftPaymentJustification(double amount, String reason) {
        if (!hasApiKey()) {
            return "[MOCK — set GEMINI_API_KEY to generate a real draft]\n\n"
                    + "Pagamento di € " + amount + " da confermare.\nMotivo indicato: " + reason
                    + "\n\nNessuna anomalia rilevata automaticamente, ma si consiglia una verifica manuale"
                    + " prima della conferma, trattandosi di un'operazione su un conto beneficiario.";
        }

        String prompt = """
                Sei un assistente che prepara pagamenti aziendali per la revisione umana.
                Scrivi una breve nota (massimo 4-5 frasi) in italiano che riassuma il pagamento
                e segnali eventuali punti da verificare, per aiutare chi deve approvarlo. Non
                approvare né rifiutare tu stesso, limitati a riassumere e segnalare cosa controllare.
                Rispondi solo con il testo della nota.

                Importo: € %.2f
                Motivo dichiarato: %s
                """.formatted(amount, reason);

        return generate(prompt, "[AI note unavailable — empty response from Gemini API]");
    }

    private boolean hasApiKey() {
        return apiKey != null && !apiKey.isBlank();
    }

    private String generate(String prompt, String emptyResponseMessage) {
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))
        );

        GeminiResponse response = restClient.post()
                .uri("/v1beta/models/{model}:generateContent?key={key}", model, apiKey)
                .header("content-type", "application/json")
                .body(requestBody)
                .retrieve()
                .body(GeminiResponse.class);

        if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
            return emptyResponseMessage;
        }
        return response.candidates().get(0).content().parts().get(0).text();
    }

    private record GeminiResponse(List<Candidate> candidates) {
    }

    private record Candidate(Content content) {
    }

    private record Content(List<Part> parts) {
    }

    private record Part(String text) {
    }
}
