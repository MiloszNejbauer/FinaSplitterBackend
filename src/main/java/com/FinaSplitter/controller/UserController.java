package com.FinaSplitter.controller;

import com.FinaSplitter.dto.CurrencyUpdateRequest;
import com.FinaSplitter.dto.LoginRequest;
import com.FinaSplitter.dto.RegisterRequest;
import com.FinaSplitter.repository.UserRepository;
import com.FinaSplitter.service.UserService;
import com.FinaSplitter.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request){
        try{
            return ResponseEntity.ok(userService.registerUser(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            String token = userService.loginAndReturnToken(loginRequest.email(), loginRequest.password());

            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Niepoprawne dane logowania");
        }
    }

    @GetMapping("/friends")
    public ResponseEntity<?> getFriends(@RequestParam String email) {
        try {
            return ResponseEntity.ok(userService.getUserFriendsData(email));
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

    @PatchMapping("/settings/currency")
    public ResponseEntity<?> updateCurrency(@RequestBody CurrencyUpdateRequest request, Authentication authentication) {
        try {
            String email = authentication.getName();
            User updatedUser = userService.updateDefaultCurrency(email, request.currency());
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userService.getUserByEmail(email);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }
}
