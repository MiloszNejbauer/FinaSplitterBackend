package com.FinaSplitter.repository;

import com.FinaSplitter.model.Group;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends MongoRepository<Group, String> {
    Optional<Group> findById(String Id);
    List<Group> findAllByMemberEmailsContaining(String email);
}
