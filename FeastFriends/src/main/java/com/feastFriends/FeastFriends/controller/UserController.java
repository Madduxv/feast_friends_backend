package com.feastFriends.feastFriends.controller;

import com.feastFriends.feastFriends.model.User;
import com.feastFriends.feastFriends.model.Friend;
import com.feastFriends.feastFriends.model.FriendRequest;
import com.feastFriends.feastFriends.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

  @Autowired
  private UserService userService;

  @GetMapping("/find_user")
  public ResponseEntity<User> findUser(@RequestBody String name) {
    return ResponseEntity.ok(userService.findUserByName(name));
  }

  @GetMapping("/get_friends")
  public ResponseEntity<List<Friend>> getFriends(@RequestParam String name) {
    return ResponseEntity.ok(userService.getFriends(name));
  }

  @GetMapping("/all_users")
  public ResponseEntity<List<User>> getAllUsers() {
    return ResponseEntity.ok(userService.getAllUsers());
  }

  @GetMapping("/all_users_names")
  public ResponseEntity<List<String>> getAllUsersNames() {
    return ResponseEntity.ok(userService.getAllUsersNames());
  }

  @PostMapping("/add_user")
  public ResponseEntity<String> addUser(@RequestBody String name) {
    userService.addUser(name);
    return ResponseEntity.ok("Added User");
  }

  @PostMapping("/add_friend")
  public ResponseEntity<String> addFriend(@RequestParam String user, @RequestParam String friend) {
    userService.addFriend(user, friend);
    return ResponseEntity.ok("Friend Added");
  }
}
