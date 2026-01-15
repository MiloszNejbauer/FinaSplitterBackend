package com.FinaSplitter.controller;

import com.FinaSplitter.model.Group;
import com.FinaSplitter.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {
    @Autowired
    private final GroupService groupService;

    public GroupController(GroupService groupService){
        this.groupService = groupService;
    }

    @PostMapping("/create")
    public ResponseEntity<Group> create(@RequestParam String name, @RequestParam String creatorEmail) {
        return ResponseEntity.ok(groupService.createGroup(name, creatorEmail));
    }

    @PostMapping("/{groupId}/add-user")
    public ResponseEntity<?> addUser(@PathVariable String groupId, @RequestParam String email) {
        try {
            return ResponseEntity.ok(groupService.addUserToGroup(groupId, email));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<List<Group>> getGroupsByUser(@PathVariable String email){
        List<Group> groups = groupService.getGroupsByUserEmail(email);
        return ResponseEntity.ok(groups);
    }
}
