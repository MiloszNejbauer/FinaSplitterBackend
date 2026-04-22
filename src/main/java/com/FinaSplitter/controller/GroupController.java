package com.FinaSplitter.controller;

import com.FinaSplitter.dto.GroupCreateRequest;
import com.FinaSplitter.model.Group;
import com.FinaSplitter.service.ExpenseService;
import com.FinaSplitter.service.GroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;
    private final ExpenseService expenseService;

    public GroupController(GroupService groupService, ExpenseService expenseService){
        this.groupService = groupService;
        this.expenseService = expenseService;
    }

    @PostMapping("/create")
    public ResponseEntity<Group> create(@RequestBody GroupCreateRequest request) {
        return ResponseEntity.ok(groupService.createGroup(request.name(), request.creatorEmail()));
    }

    @PostMapping("/{groupId}/add-user")
    public ResponseEntity<?> addUser(@PathVariable String groupId, @RequestParam String email) {
        try {
            return ResponseEntity.ok(groupService.addUserToGroup(groupId, email));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<List<Group>> getGroupsByUser(@PathVariable String email){
        List<Group> groups = groupService.getGroupsByUserEmail(email);
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Group> getGroupById(@PathVariable String id) {
        return ResponseEntity.ok(groupService.getGroupById(id));
    }

    @GetMapping("/user/{email}/with-balances")
    public ResponseEntity<List<Map<String, Object>>> getGroupsWithBalances(@PathVariable String email){
        List<Group> groups = groupService.getGroupsByUserEmail(email);

        List<Map<String, Object>> response = groups.stream().map(group -> {
            Map<String, Object> groupMap = new HashMap<>();
            groupMap.put("id", group.getId());
            groupMap.put("name", group.getName());
            groupMap.put("members", group.getMembers());

            Map<String, Map<String, Double>> allBalances = expenseService.calculateBalances(group.getId());

            Map<String, Double> userCurrencyBalances = allBalances.getOrDefault(email, new HashMap<>());

            double totalInBase = 0.0;

            groupMap.put("balances", userCurrencyBalances);

            groupMap.put("userBalance", totalInBase);

            return groupMap;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{groupId}/convert")
    public ResponseEntity<?> convertGroupExpenses(@PathVariable String groupId, @RequestParam String targetCurrency) {
        try {
            expenseService.convertAllGroupExpensesToCurrency(groupId, targetCurrency.toUpperCase());
            return ResponseEntity.ok(Map.of("message", "Przeliczono na " + targetCurrency));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/group/{groupId}/member-spending-summary")
    public ResponseEntity<Map<String, Double>> getMemberSpendingSummary(@PathVariable String groupId) {
        try {
            return ResponseEntity.ok(expenseService.getMemberSpendingSummary(groupId));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(null);
        }
    }
}