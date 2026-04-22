package com.FinaSplitter.controller;

import com.FinaSplitter.dto.DebtSettlement;
import com.FinaSplitter.dto.ExpenseRequest;
import com.FinaSplitter.model.Expense;
import com.FinaSplitter.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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
            Expense expense = expenseService.addExpense(request);
            return ResponseEntity.ok(expense);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/group/{groupId}")
    public List<Expense> getByGroup(@PathVariable String groupId){
        return expenseService.getExpensesByGroup(groupId);
    }

    @GetMapping("/group/{groupId}/balances")
    public ResponseEntity<Map<String, Map<String, Double>>> getGroupBalances(@PathVariable String groupId) {
        return ResponseEntity.ok(expenseService.calculateBalances(groupId));
    }

    @GetMapping("/user/{email}/total-balance")
    public ResponseEntity<Double> getTotalBalance(@PathVariable String email) {
        try {
            Double total = expenseService.calculateTotalBalancesForUser(email);
            return ResponseEntity.ok(total);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/settle")
    public ResponseEntity<?> settleDebt(@RequestParam String groupId,
                                        @RequestParam String fromEmail,
                                        @RequestParam String toEmail,
                                        @RequestParam Double amount,
                                        @RequestParam String currency) {
        try {
            ExpenseRequest settlementRequest = new ExpenseRequest();
            settlementRequest.setGroupId(groupId);
            settlementRequest.setDescription("Rozliczenie: " + fromEmail + " -> " + toEmail);
            settlementRequest.setTotalAmount(amount);
            settlementRequest.setPaidById(fromEmail);
            settlementRequest.setIsSettlement(true);
            settlementRequest.setCurrency(currency);

            Map<String, Double> shares = new HashMap<>();
            shares.put(toEmail, amount);
            settlementRequest.setParticipantShares(shares);

            return ResponseEntity.ok(expenseService.addExpense(settlementRequest));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @GetMapping("/user/{email}/monthly-summary")
    public ResponseEntity<Map<String, Double>> getMonthlySummary(@PathVariable String email) {
        try {
            return ResponseEntity.ok(expenseService.getUserMonthlyBalances(email));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/group/{groupId}/debts")
    public ResponseEntity<List<DebtSettlement>> getGroupDebts(@PathVariable String groupId) {
        try {
            List<DebtSettlement> debts = expenseService.calculateExactDebts(groupId);
            return ResponseEntity.ok(debts);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}