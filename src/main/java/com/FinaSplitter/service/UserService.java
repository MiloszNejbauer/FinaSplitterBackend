package com.FinaSplitter.service;

import com.FinaSplitter.model.User;
import com.FinaSplitter.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

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

    public Set<String> getUserFriends(String email) throws Exception {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new Exception("Użytkownik nie istnieje"));

        return user.getFriends();
    }

    public void addFriend(String currentUserEmail, String friendEmail) throws Exception {
        if (currentUserEmail.equalsIgnoreCase(friendEmail)) {
            throw new Exception("Nie możesz dodać samego siebie do znajomych");
        }

        User currentUser = userRepository.findByEmailIgnoreCase(currentUserEmail)
                .orElseThrow(() -> new Exception("Twoja sesja wygasła lub użytkownik nie istnieje"));

        User friend = userRepository.findByEmailIgnoreCase(friendEmail)
                .orElseThrow(() -> new Exception("Użytkownik o podanym adresie e-mail nie istnieje w systemie"));

        // Dodajemy nawzajem (relacja dwustronna)
        currentUser.getFriends().add(friend.getEmail());
        friend.getFriends().add(currentUser.getEmail());

        userRepository.save(currentUser);
        userRepository.save(friend);
    }
}
