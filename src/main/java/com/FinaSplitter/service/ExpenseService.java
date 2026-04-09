package com.FinaSplitter.service;

import com.FinaSplitter.dto.ExpenseRequest;
import com.FinaSplitter.model.Group;
import com.FinaSplitter.model.User;
import com.FinaSplitter.repository.ExpenseRepository;
import com.FinaSplitter.model.Expense;
import com.FinaSplitter.repository.GroupRepository;

import com.FinaSplitter.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CurrencyService currencyService;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    @Autowired
    public ExpenseService(ExpenseRepository expenseRepository,
                          GroupRepository groupRepository,
                          CurrencyService currencyService,
                          UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.groupRepository = groupRepository;
        this.currencyService = currencyService;
        this.userRepository = userRepository;
    }

    public Expense addExpense(ExpenseRequest request) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User creator = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono zalogowanego użytkownika"));

        String expenseCurrency = creator.getDefaultCurrency() != null ? creator.getDefaultCurrency().toUpperCase() : "PLN";

        if (request.getCurrency() != null && !request.getCurrency().trim().isEmpty()) {
            expenseCurrency = request.getCurrency().toUpperCase();
        }

        if (request.getParticipantShares() == null || request.getParticipantShares().isEmpty()) {
            throw new RuntimeException("Wydatek musi mieć przypisanych uczestników!");
        }

        double sumOfShares = request.getParticipantShares().values().stream()
                .mapToDouble(Double::doubleValue).sum();

        if (Math.abs(request.getTotalAmount() - sumOfShares) > 0.01) {
            throw new RuntimeException("Suma udziałów nie zgadza się z kwotą całkowitą!");
        }

        double rateToPLN = currencyService.getExchangeRate(expenseCurrency);

        Expense expense = new Expense();
        expense.setDescription(request.getDescription());
        expense.setPaidById(request.getPaidById());
        expense.setGroupId(request.getGroupId());
        expense.setIsSettlement(request.getIsSettlement());
        expense.setCreatedAt(LocalDateTime.now());

        expense.setCurrency(expenseCurrency);
        expense.setTotalAmount(request.getTotalAmount());
        expense.setParticipants(request.getParticipantShares());

        expense.setExchangeRateAtTime(rateToPLN);
        expense.setAmountInPln(request.getTotalAmount() * rateToPLN);

        return expenseRepository.save(expense);
    }

    public Map<String, Map<String, Double>> calculateBalances(String groupId) {
        List<Expense> expenses = expenseRepository.findAllByGroupIdOrderByCreatedAtDesc(groupId);
        Map<String, Map<String, Double>> balances = new HashMap<>();

        for (Expense expense : expenses) {
            String currency = expense.getCurrency() != null ? expense.getCurrency() : "PLN";
            String payer = expense.getPaidById() != null ? expense.getPaidById() : "nieznany@uzytkownik";

            balances.computeIfAbsent(payer, k -> new HashMap<>());
            Map<String, Double> payerMap = balances.get(payer);
            payerMap.put(currency, payerMap.getOrDefault(currency, 0.0) + expense.getTotalAmount());

            if (expense.getParticipants() != null) {
                for (Map.Entry<String, Double> entry : expense.getParticipants().entrySet()) {
                    String participant = entry.getKey() != null ? entry.getKey() : "nieznany@uzytkownik";
                    double share = entry.getValue() != null ? entry.getValue() : 0.0;

                    balances.computeIfAbsent(participant, k -> new HashMap<>());
                    Map<String, Double> partMap = balances.get(participant);
                    partMap.put(currency, partMap.getOrDefault(currency, 0.0) - share);
                }
            }
        }
        return balances;
    }

    public Double calculateTotalBalancesForUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        String userBaseCurrency = user.getDefaultCurrency();

        List<Group> userGroups = groupRepository.findAllByMembersEmail(email);
        double totalBalanceInBaseCurrency = 0.0;

        for (Group group : userGroups) {
            Map<String, Map<String, Double>> groupBalances = calculateBalances(group.getId());

            Map<String, Double> userCurrencyBalances = groupBalances.getOrDefault(email, new HashMap<>());

            for (Map.Entry<String, Double> entry : userCurrencyBalances.entrySet()) {
                String currency = entry.getKey();
                Double amount = entry.getValue();

                totalBalanceInBaseCurrency += currencyService.convert(amount, currency, userBaseCurrency);
            }
        }
        return totalBalanceInBaseCurrency;
    }

    public List<Expense> getExpensesByGroup(String groupId) {
        return expenseRepository.findAllByGroupIdOrderByCreatedAtDesc(groupId);
    }

    public Map<String, Double> getUserMonthlyBalances(String email) {
        List<Group> userGroups = groupRepository.findAllByMembersEmail(email);
        Map<String, Double> monthlyBalances = new TreeMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        for (Group group : userGroups) {
            List<Expense> expenses = expenseRepository.findAllByGroupIdOrderByCreatedAtDesc(group.getId());

            for (Expense expense : expenses) {
                if (Boolean.TRUE.equals(expense.getIsSettlement()) || expense.getCreatedAt() == null) continue;

                String monthYear = expense.getCreatedAt().format(formatter);

                double rate = expense.getExchangeRateAtTime() != null ? expense.getExchangeRateAtTime() : 1.0;
                Double userShareOriginal = expense.getParticipants().get(email);

                if (userShareOriginal != null) {
                    double userShareInPln = userShareOriginal * rate;
                    monthlyBalances.put(monthYear, monthlyBalances.getOrDefault(monthYear, 0.0) + userShareInPln);
                }
            }
        }
        return monthlyBalances;
    }

    @Transactional
    public void convertAllGroupExpensesToCurrency(String groupId, String targetCurrency) {
        List<Expense> expenses = expenseRepository.findAllByGroupIdOrderByCreatedAtDesc(groupId);

        for (Expense expense : expenses) {
            String fromCurrency = expense.getCurrency();
            if (fromCurrency.equalsIgnoreCase(targetCurrency)) continue;

            double rateFrom = currencyService.getExchangeRateForDate(fromCurrency, expense.getCreatedAt());
            double rateTo = currencyService.getExchangeRateForDate(targetCurrency, expense.getCreatedAt());

            double amountInBase = expense.getTotalAmount() * rateFrom;
            double newTotal = amountInBase / rateTo;

            expense.setTotalAmount(newTotal);
            expense.setCurrency(targetCurrency);

            Map<String, Double> newParticipants = new HashMap<>();
            for (Map.Entry<String, Double> entry : expense.getParticipants().entrySet()) {
                double participantAmountInBase = entry.getValue() * rateFrom;
                newParticipants.put(entry.getKey(), participantAmountInBase / rateTo);
            }
            expense.setParticipants(newParticipants);

            expense.setExchangeRateAtTime(rateTo);
            expense.setAmountInPln(amountInBase);

            expenseRepository.save(expense);
        }
    }
}