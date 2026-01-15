package com.FinaSplitter.repository;

import com.FinaSplitter.model.Expense;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ExpenseRepository extends MongoRepository<Expense, String> {
    List<Expense> findAllByGroupId(String groupId);
}
