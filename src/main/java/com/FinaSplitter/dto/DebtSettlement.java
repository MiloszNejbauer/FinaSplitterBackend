package com.FinaSplitter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DebtSettlement {
    private String fromUserEmail;
    private String toUserEmail;
    private Double amount;
    private String currency;
    private String fromUserName;
    private String toUserName;
}