package com.FinaSplitter.service;

import com.FinaSplitter.model.Group;
import com.FinaSplitter.model.GroupMember;
import com.FinaSplitter.model.User;
import com.FinaSplitter.repository.GroupRepository;
import com.FinaSplitter.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
public class GroupService {
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private UserRepository userRepository;

    public Group createGroup(String name, String creatorEmail){
        User creator = userRepository.findByEmailIgnoreCase(creatorEmail).orElseThrow(() -> new RuntimeException("Nie znaleziono twórcy"));

        GroupMember member = new GroupMember(creator.getEmail(), creator.getUsername());
        Group group = new Group(name, member);
        return groupRepository.save(group);
    }

    public Group addUserToGroup(String groupId, String email) throws Exception {
        User newUser = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new Exception("Użytkownik o takim mailu nie istnieje"));

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new Exception("Grupa nie znaleziona"));

        // Sprawdzamy czy już jest w grupie (po mailu)
        boolean alreadyMember = group.getMembers().stream()
                .anyMatch(m -> m.getEmail().equalsIgnoreCase(newUser.getEmail()));

        if (alreadyMember) {
            throw new Exception("Użytkownik jest już członkiem tej grupy");
        }

        // Automatyczne dodawanie znajomych (używamy maili do relacji)
        for (GroupMember member : group.getMembers()) {
            updateFriendship(newUser.getEmail(), member.getEmail());
            updateFriendship(member.getEmail(), newUser.getEmail());
        }

        // Dodajemy pełny obiekt GroupMember
        group.getMembers().add(new GroupMember(newUser.getEmail(), newUser.getUsername()));
        return groupRepository.save(group);
    }

    private void updateFriendship(String userEmail, String friendEmail) {
        userRepository.findByEmailIgnoreCase(userEmail).ifPresent(user -> {
            if (user.getFriends() == null) {
                user.setFriends(new HashSet<>());
            }
            user.getFriends().add(friendEmail);
            userRepository.save(user);
        });
    }

    public List<Group> getGroupsByUserEmail(String email) {
        // Musisz zaktualizować GroupRepository, aby szukało wewnątrz listy obiektów
        return groupRepository.findAllByMembersEmail(email);
    }

    public Group getGroupById(String id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono grupy o id: " + id));
    }
}