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

  // public UserController(UserService userService) {
  //   this.userService = userService;
  // }

  @GetMapping("/find_user")
  public ResponseEntity<User> findUser(@RequestBody String name) {
    return ResponseEntity.ok(userService.findUserByName(name));
  }

  @GetMapping("/get_friends")
  public ResponseEntity<List<Friend>> getFriends(@RequestBody String name) {
    return ResponseEntity.ok(userService.getFriends(name));
  }

  @GetMapping("/all_users")
  public ResponseEntity<List<User>> getAllUsers() {
    return ResponseEntity.ok(userService.getAllUsers());
  }

  @PostMapping("/add_user")
  public ResponseEntity<String> addUser(@RequestBody String name) {
    userService.addUser(name);
    return ResponseEntity.ok("Added User");
  }

  @PostMapping("/add_friend")
  public ResponseEntity<String> addFriend(@RequestBody FriendRequest request) {
    userService.addFriend(request.getUser(), request.getFriendName());
    userService.addFriend(request.getFriendName(), request.getUser());
    return ResponseEntity.ok("Friend Added");
  }
}