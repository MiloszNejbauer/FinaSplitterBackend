package com.FinaSplitter.service;

import com.FinaSplitter.dto.RegisterRequest;
import com.FinaSplitter.model.User;
import com.FinaSplitter.repository.UserRepository;
import com.FinaSplitter.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, JwtUtils jwtUtils, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(RegisterRequest request) throws Exception {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new Exception("Użytkownik o podanym adresie email już istnieje!");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email().toLowerCase());

        user.setPassword(passwordEncoder.encode(request.password()));

        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }

        return userRepository.save(user);
    }

    public User login(String email, String password) throws Exception{
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new Exception("Nie znaleziono użytkownika"));

        if (!user.getPassword().equals(password)){
            throw new Exception("Nieprawidłowe hasło");
        }
        return user;
    }

    // Zmień typ zwracany z Set<String> na listę obiektów User (lub dedykowane FriendDTO)
    public List<Map<String, String>> getUserFriendsData(String email) throws Exception {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new Exception("Użytkownik nie istnieje"));

        List<Map<String, String>> friendsData = new ArrayList<>();
        for (String friendEmail : user.getFriends()) {
            userRepository.findByEmailIgnoreCase(friendEmail).ifPresent(f -> {
                Map<String, String> data = new HashMap<>();
                data.put("username", f.getUsername());
                data.put("email", f.getEmail());
                friendsData.add(data);
            });
        }
        return friendsData;
    }

    public void addFriend(String currentUserEmail, String friendEmail) throws Exception {
        if (currentUserEmail.equalsIgnoreCase(friendEmail)) {
            throw new Exception("Nie możesz dodać samego siebie do znajomych");
        }

        User currentUser = userRepository.findByEmailIgnoreCase(currentUserEmail)
                .orElseThrow(() -> new Exception("Użytkownik nie istnieje"));

        User friend = userRepository.findByEmailIgnoreCase(friendEmail)
                .orElseThrow(() -> new Exception("Użytkownik o podanym adresie e-mail nie istnieje w systemie"));

        // Zabezpieczenie przed Null na liście znajomych
        if (currentUser.getFriends() == null) currentUser.setFriends(new HashSet<>());
        if (friend.getFriends() == null) friend.setFriends(new HashSet<>());

        currentUser.getFriends().add(friend.getEmail());
        friend.getFriends().add(currentUser.getEmail());

        userRepository.save(currentUser);
        userRepository.save(friend);
    }

    public String loginAndReturnToken(String email, String password) throws Exception {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new Exception("Nie znaleziono użytkownika"));

        // Bezpieczne porównanie hasła przesłanego z zahaszowanym hasłem w bazie
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new Exception("Nieprawidłowe hasło");
        }

        return jwtUtils.generateToken(user.getEmail());
    }

    // W klasie UserService
    public User updateDefaultCurrency(String email, String newCurrency) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("Użytkownik nie znaleziony"));

        // Walidacja - upewnij się, że waluta jest wielkimi literami i nie jest nullem
        if (newCurrency == null || newCurrency.isBlank()) {
            throw new RuntimeException("Waluta nie może być pusta");
        }

        user.setDefaultCurrency(newCurrency.toUpperCase().trim());
        return userRepository.save(user);
    }

    // W klasie UserService.java
    public User getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("Użytkownik o mailu " + email + " nie istnieje."));
    }
}
