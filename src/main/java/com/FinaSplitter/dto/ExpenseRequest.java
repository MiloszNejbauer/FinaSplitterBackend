package com.FinaSplitter.dto;

import lombok.Data;

import java.util.Map;

@Data
public class ExpenseRequest {

    private String description;
    private double totalAmount;
    private String paidById;
    private String groupId;
    private Map<String, Double> participantShares;
}
