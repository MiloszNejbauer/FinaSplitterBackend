package com.FinaSplitter.service;

import com.FinaSplitter.model.Group;
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
        Group group = new Group(name, creatorEmail);
        return groupRepository.save(group);
    }

    public Group addUserToGroup(String groupId, String email) throws Exception {
        User newUser = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new Exception("Użytkownik o takim mailu nie istnieje"));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new Exception("Grupa nie znaleziona"));

        String officialEmail = newUser.getEmail();

        if (group.getMemberEmails().contains(officialEmail)) {
            throw new Exception("Użytkownik jest już członkiem tej grupy");
        }

        // Automatyczne dodawanie znajomych
        List<String> currentMembers = group.getMemberEmails();
        for (String memberEmail : currentMembers) {
            updateFriendship(officialEmail, memberEmail);
            updateFriendship(memberEmail, officialEmail);
        }

        group.getMemberEmails().add(officialEmail);
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

    public List<Group> getGroupsByUserEmail(String email){
        return groupRepository.findAllByMemberEmailsContaining(email);
    }

    public Group getGroupById(String id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono grupy o id: " + id));
    }
}