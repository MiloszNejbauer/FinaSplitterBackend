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
        if (apiKey == null || apiKey.isEmpty() || apiKey.contains("google.api.key")) {
            System.err.println("BŁĄD: Brak klucza API Google w application.properties!");
            return "{\"error\": \"Brak konfiguracji AI na serwerze\"}";
        }

        String prompt = "Przeanalizuj to zdjęcie paragonu i zwróć dane w formacie JSON. " +
                "Struktura: { " +
                "\"storeName\": \"string\", " +
                "\"totalAmount\": double, " +
                "\"currency\": \"string (kod ISO 4217, np. PLN, EUR, USD)\", " +
                "\"date\": \"YYYY-MM-DD\", " +
                "\"items\": [ " +
                "  { \"name\": \"nazwa produktu\", \"price\": cena_jednostkowa_double, \"quantity\": ilosc_double } " +
                "] " +
                "}. " +
                "Zwróć TYLKO czysty JSON. Jeśli nazwa produktu jest ucięta, spróbuj ją uzupełnić logicznie." +
                "Zwróć uwagę na symbole walut przy kwocie łącznej. Jeśli waluta nie jest oczywista, przyjmij PLN.";

        try {
            Client client = Client.builder().apiKey(apiKey).build();

            Content content = Content.fromParts(
                    Part.fromText(prompt),
                    Part.fromBytes(imageBytes, "image/jpeg")
            );

            GenerateContentResponse response = client.models.generateContent("gemini-2.5-flash", content, null);

            String text = response.text();

            if (text == null || text.isEmpty()) {
                return "{}";
            }

            return text.replace("```json", "").replace("```", "").trim();

        } catch (Exception e) {
            System.err.println("Błąd SDK Gemini: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Błąd komunikacji z modelem: " + e.getMessage());
        }
    }
}