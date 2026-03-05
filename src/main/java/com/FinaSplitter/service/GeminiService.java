package com.FinaSplitter.service;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GeminiService {

    @Value("${google.api.key}")
    private String apiKey;

    public String scanReceipt(byte[] imageBytes) {
        // 1. Zabezpieczenie przed brakiem klucza
        if (apiKey == null || apiKey.isEmpty() || apiKey.contains("google.api.key")) {
            System.err.println("BŁĄD: Brak klucza API Google w application.properties!");
            return "{\"error\": \"Brak konfiguracji AI na serwerze\"}";
        }

        // 2. Prosty prompt testowy na początek
        String prompt = "Przeanalizuj to zdjęcie paragonu i zwróć dane w formacie JSON: " +
                "{ \"totalAmount\": 0.00, \"storeName\": \"string\" }. " +
                "Zwróć TYLKO kod JSON.";

        try {
            // 3. Inicjalizacja oficjalnego klienta SDK
            Client client = Client.builder().apiKey(apiKey).build();

            // 4. Budowanie zawartości (tekst + zdjęcie) za pomocą wbudowanych klas SDK
            Content content = Content.fromParts(
                    Part.fromText(prompt),
                    Part.fromBytes(imageBytes, "image/jpeg")
            );

            // 5. Wywołanie modelu (używamy stabilnego gemini-2.5-flash)
            GenerateContentResponse response = client.models.generateContent("gemini-2.5-flash", content, null);

            // 6. Odczyt tekstu - SDK samo obsługuje zawiłą strukturę odpowiedzi!
            String text = response.text();

            if (text == null || text.isEmpty()) {
                return "{}"; // Pusta odpowiedź awaryjna
            }

            // Oczyszczenie, gdyby model dodał np. ```json ... ```
            return text.replace("```json", "").replace("```", "").trim();

        } catch (Exception e) {
            System.err.println("Błąd oficjalnego SDK Gemini: " + e.getMessage());
            e.printStackTrace();
            return "{\"error\": \"Błąd komunikacji z modelem: " + e.getMessage() + "\"}";
        }
    }
}