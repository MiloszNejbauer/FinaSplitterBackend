package com.FinaSplitter.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class GroupBalanceDTO {
    private Map<String, Double> userBalances;
}
