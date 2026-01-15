package com.FinaSplitter.controller;

import com.FinaSplitter.dto.ExpenseRequest;
import com.FinaSplitter.model.Expense;
import com.FinaSplitter.service.ExpenseService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/expenses")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @PostMapping("/add")
    public ResponseEntity<?> addExpense(@RequestBody ExpenseRequest request) {
        try {
            // Przekazujemy cały obiekt request do serwisu
            Expense expense = expenseService.addExpense(request);
            return ResponseEntity.ok(expense);
        } catch (Exception e) {
            // Jeśli walidacja w serwisie zawiedzie, zwrócimy błąd 400 z opisem
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/group/{groupId}")
    public List<Expense> getByGroup(@PathVariable String groupId){
        return expenseService.getExpensesByGroup(groupId);
    }

    @GetMapping("/group/{groupId}/balances")
    public ResponseEntity<Map<String, Double>> getGroupBalances(@PathVariable String groupId) {
        return ResponseEntity.ok(expenseService.calculateBalances(groupId));
    }
}