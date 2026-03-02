package com.FinaSplitter.controller;

import com.FinaSplitter.repository.UserRepository;
import com.FinaSplitter.service.UserService;
import com.FinaSplitter.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user){
        try{
            return ResponseEntity.ok(userService.registerUser(user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String password){
        try{
            return ResponseEntity.ok(userService.login(email, password));
        } catch (Exception e){
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @GetMapping("/friends")
    public ResponseEntity<?> getFriends(@RequestParam String email) {
        try {
            Set<String> friends = userService.getUserFriends(email);
            return ResponseEntity.ok(friends);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PostMapping("/friends/add")
    public ResponseEntity<?> addFriend(@RequestParam String email, @RequestParam String friendEmail) {
        try {
            userService.addFriend(email, friendEmail);
            return ResponseEntity.ok("Dodano do znajomych!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
