package com.FinaSplitter.service;

import com.FinaSplitter.dto.ExpenseRequest;
import com.FinaSplitter.repository.ExpenseRepository;
import com.FinaSplitter.model.Expense;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExpenseService {
    @Autowired
    private ExpenseRepository expenseRepository;

    public Expense addExpense(ExpenseRequest request) {
        // 1. Walidacja: Sumujemy wszystkie kwoty z mapy udziałów
        double sumOfShares = request.getParticipantShares().values()
                .stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        // Sprawdzamy, czy suma udziałów równa się totalAmount (z tolerancją na błędy zaokrągleń)
        if (Math.abs(request.getTotalAmount() - sumOfShares) > 0.01) {
            throw new RuntimeException("Suma udziałów (" + sumOfShares +
                    ") nie zgadza się z kwotą całkowitą (" + request.getTotalAmount() + ")!");
        }

        // 2. Mapowanie na model Expense
        Expense expense = new Expense();
        expense.setDescription(request.getDescription());
        expense.setTotalAmount(request.getTotalAmount());
        expense.setPaidById(request.getPaidById());
        expense.setGroupId(request.getGroupId());
        expense.setParticipants(request.getParticipantShares());

        return expenseRepository.save(expense);
    }

    public List<Expense> getExpensesByGroup(String groupId) {
        return expenseRepository.findAllByGroupId(groupId);
    }

    public Map<String, Double> calculateBalances(String groupId) {
        List<Expense> expenses = expenseRepository.findAllByGroupId(groupId);
        Map<String, Double> balances = new HashMap<>();

        for (Expense expense : expenses) {
            String payer = expense.getPaidById();
            double amount = expense.getTotalAmount();

            balances.put(payer, balances.getOrDefault(payer, 0.0) + amount);

            for (Map.Entry<String, Double> entry : expense.getParticipants().entrySet()) {
                String participant = entry.getKey();
                double debtPart = entry.getValue();

                balances.put(participant, balances.getOrDefault(participant, 0.0) - debtPart);
            }
        }
        return balances;
    }
}
