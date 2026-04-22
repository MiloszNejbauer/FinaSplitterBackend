package com.FinaSplitter.controller;

import com.FinaSplitter.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/expenses")
public class ReceiptController {

    @Autowired
    private GeminiService geminiService;

    @PostMapping("/scan")
    public ResponseEntity<?> scanReceipt(@RequestParam("file") MultipartFile file) {
        try {
            String jsonResult = geminiService.scanReceipt(file.getBytes());
            return ResponseEntity.ok(jsonResult);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Błąd przetwarzania pliku.\"}");
        }
    }
}
