package com.FinaSplitter.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.List;

@Service
public class CurrencyService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Cacheable(value = "currencies", key = "#currencyCode")
    public double getExchangeRate(String currencyCode) {
        if (currencyCode == null || currencyCode.equalsIgnoreCase("PLN")) {
            return 1.0;
        }

        String url = "http://api.nbp.pl/api/exchangerates/rates/a/" + currencyCode.toLowerCase() + "/?format=json";

        try {
            System.out.println(">>> Pobieranie kursu " + currencyCode + " bezpośrednio z API NBP...");
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.containsKey("rates")) {
                List<Map<String, Object>> rates = (List<Map<String, Object>>) response.get("rates");
                return (double) rates.get(0).get("mid");
            }
            return 1.0;
        } catch (Exception e) {
            System.err.println("Błąd NBP dla " + currencyCode + ": " + e.getMessage());
            return 1.0;
        }
    }

    public double convert(double amount, String fromCurrency, String toCurrency) {
        if (fromCurrency == null) fromCurrency = "PLN";
        if (toCurrency == null) toCurrency = "PLN";

        if (fromCurrency.equalsIgnoreCase(toCurrency)) return amount;

        double rateFrom = getExchangeRate(fromCurrency);
        double rateTo = getExchangeRate(toCurrency);

        return (amount * rateFrom) / rateTo;
    }

    @Cacheable(value = "historical_currencies", key = "#currencyCode + '_' + #date")
    public double getExchangeRateForDate(String currencyCode, LocalDateTime date) {
        if (currencyCode == null || currencyCode.equalsIgnoreCase("PLN")) {
            return 1.0;
        }

        String formattedDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String url = "http://api.nbp.pl/api/exchangerates/rates/a/" +
                currencyCode.toLowerCase() + "/" + formattedDate + "/?format=json";

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("rates")) {
                List<Map<String, Object>> rates = (List<Map<String, Object>>) response.get("rates");
                return (double) rates.get(0).get("mid");
            }
        } catch (Exception e) {
            System.err.println("Brak kursu historycznego dla " + currencyCode + " z dnia " + formattedDate + ". Pobieram bieżący.");
            return getExchangeRate(currencyCode);
        }
        return 1.0;
    }
}