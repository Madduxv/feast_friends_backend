package com.feastFriends.feastFriends.service;

import com.feastFriends.feastFriends.model.User;
import com.feastFriends.feastFriends.model.Friend;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.IOException;

public class UserService {

  ObjectMapper objectMapper = new ObjectMapper();
  File file = new File("/users.json");

  // generate id for new user

  // get user by id 
 
  private static User findUserByName(List<User> users, String name) {
    for (User user : users) {
      if (user.getName().equals(name)) {
        return user;
      }
    }
    return null;
  }

  public void addUser(Long ID, String Name) {
    try {
      List<User> users = objectMapper.readValue(file, new TypeReference<List<User>>() {});
      if (findUserByName(users, Name) == null) {
        List<Friend> emptyList = new ArrayList<>();
        User user = new User(ID, Name, emptyList); 
        objectMapper.writeValue(file, user);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public List<Friend> getFriends(String user, Long ID) {
    try {
      List<User> users = objectMapper.readValue(file, new TypeReference<List<User>>() {});
      User targetUser = findUserByName(users, user);
      if (targetUser != null) {
        return targetUser.getFriends();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public void addFriend(String user, Long ID, String friend) {
    try {
      List<User> users = objectMapper.readValue(file, new TypeReference<List<User>>() {});
      User targetUser = findUserByName(users, user);
      if (targetUser != null) {
        targetUser.addFriend(new Friend(ID, friend));
      }
      objectMapper.writeValue(file, users);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  // remove friend
  
}
