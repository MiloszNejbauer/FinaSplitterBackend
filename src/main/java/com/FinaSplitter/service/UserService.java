package com.FinaSplitter.service;

import com.FinaSplitter.model.User;
import com.FinaSplitter.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User registerUser(User user) throws Exception{
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new Exception("Użytkownik o podanym adresie email już istnieje!");
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
}
