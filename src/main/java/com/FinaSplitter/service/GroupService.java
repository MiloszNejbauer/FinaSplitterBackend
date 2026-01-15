package com.FinaSplitter.service;

import com.FinaSplitter.model.Group;
import com.FinaSplitter.repository.GroupRepository;
import com.FinaSplitter.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        if (userRepository.findByEmail(email).isEmpty()){
            throw new Exception("Użytkownik o takim mailu nie istnieje");
        }
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new Exception("Grupa nie znaleziona"));
        if (!group.getMemberEmails().contains(email)) {
            group.getMemberEmails().add(email);
        }
        return groupRepository.save(group);
    }

    public List<Group> getGroupsByUserEmail(String email){
        return groupRepository.findAllByMemberEmailsContaining(email);
    }
}