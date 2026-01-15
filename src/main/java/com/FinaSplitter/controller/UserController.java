package com.FinaSplitter.controller;

import com.FinaSplitter.service.UserService;
import com.FinaSplitter.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

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
}
