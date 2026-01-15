package com.FinaSplitter.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Data
@Document
public class Expense {

    @Id
    private String id;
    private String description;
    private double totalAmount;
    private String paidById;
    private String groupId;

    private Map<String, Double> participants;
}
