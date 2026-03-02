package com.FinaSplitter.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@Document
public class User {

    @Id
    private String id;
    private  String username;

    @Indexed(unique = true)
    private String email;

    private  String password;
    private Set<String> friends = new HashSet<>();

    public User(String username, String email, String password){
        this.username = username;
        this.email = email;
        this.password = password;
        this.friends = new HashSet<>();
    }
}
