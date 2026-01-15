package com.FinaSplitter.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "groups")
@NoArgsConstructor(onConstructor_ = {@PersistenceCreator})
public class Group {

    @Id
    private String id;
    private String name;
    private List<String> memberEmails = new ArrayList<>();

    public Group(String name, String creatorEmail) {
        this.name = name;
        this.memberEmails = new ArrayList<>();
        this.memberEmails.add(creatorEmail);
    }

}
