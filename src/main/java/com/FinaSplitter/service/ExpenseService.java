package com.FinaSplitter.service;

import com.FinaSplitter.dto.ExpenseRequest;
import com.FinaSplitter.model.Group;
import com.FinaSplitter.repository.ExpenseRepository;
import com.FinaSplitter.model.Expense;
import com.FinaSplitter.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
        expense.setIsSettlement(request.getIsSettlement());
        expense.setCreatedAt(LocalDateTime.now());

        return expenseRepository.save(expense);
    }

    public List<Expense> getExpensesByGroup(String groupId) {
        return expenseRepository.findAllByGroupIdOrderByCreatedAtDesc(groupId);
    }

    public Map<String, Double> calculateBalances(String groupId) {
        List<Expense> expenses = expenseRepository.findAllByGroupIdOrderByCreatedAtDesc(groupId);
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

    @Autowired
    private GroupRepository groupRepository;

    public Double calculateTotalBalancesForUser(String email) {
        List<Group> userGroups = groupRepository.findAllByMembersEmail(email);

        double totalBalance = 0.0;

        for (Group group : userGroups) {
            Map<String, Double> groupBalances = calculateBalances(group.getId());
            totalBalance += groupBalances.getOrDefault(email, 0.0);
        }
        return totalBalance;
    }

    // ExpenseService.java

    public Map<String, Double> getUserMonthlyBalances(String email) {
        List<Group> userGroups = groupRepository.findAllByMembersEmail(email);
        // TreeMap z customowym komparatorem, aby sortować daty (opcjonalne)
        Map<String, Double> monthlyBalances = new TreeMap<>();

        for (Group group : userGroups) {
            List<Expense> expenses = expenseRepository.findAllByGroupIdOrderByCreatedAtDesc(group.getId());

            for (Expense expense : expenses) {
                // Omijamy rozliczenia (settlements), jeśli chcesz widzieć tylko konsumpcję
                if (expense.getIsSettlement()) continue;

                // ZABEZPIECZENIE: Jeśli wydatek nie ma daty, użyj daty dzisiejszej lub pomiń
                String monthYear;
                if (expense.getCreatedAt() != null) {
                    monthYear = expense.getCreatedAt().getMonth().toString() + " " + expense.getCreatedAt().getYear();
                } else {
                    monthYear = "UNKNOWN"; // Lub np. LocalDateTime.now().getMonth().toString...
                }

                // Pobieramy udział użytkownika z mapy participants
                // participants: { "email": kwota }
                Double userShare = expense.getParticipants().get(email);

                if (userShare != null) {
                    monthlyBalances.put(monthYear, monthlyBalances.getOrDefault(monthYear, 0.0) + userShare);
                }
            }
        }
        return monthlyBalances;
    }
}
