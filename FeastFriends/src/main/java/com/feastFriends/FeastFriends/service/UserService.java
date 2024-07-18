package com.feastFriends.feastFriends.service;

import com.feastFriends.feastFriends.model.User;
import com.feastFriends.feastFriends.model.Friend;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  ObjectMapper objectMapper;
  private File file;
  private List<User> users;

  public UserService(@Value("${file.path}") String filePath) {
    this.objectMapper = new ObjectMapper();
    this.file = new File(filePath);
    this.users = deserialize();
  }

  private List<User> deserialize() {
    try {
      if (file.exists()) {
        return objectMapper.readValue(file, new TypeReference<List<User>>() {});
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new ArrayList<>();
  }

  private void serialize() {
    try {
      objectMapper.writeValue(file, users);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
 
  public User findUserByName(String name) {
    for (User user : users) {
      if (user.getName().equals(name)) {
        return user;
      }
    }
    return null;
  }

  public void addUser(String name) {
    try {
      users = deserialize(); // Refresh users list
      if (findUserByName(name) == null) {
        List<Friend> emptyList = new ArrayList<>();
        User user = new User(name, emptyList);
        users.add(user);
        serialize(); // Serialize the updated users list
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public List<Friend> getFriends(String name) {
    User targetUser = findUserByName(name);
    if (targetUser != null) {
      return targetUser.getFriends();
    }
    return null;
  }

  public void addFriend(String user, String friend) {
    try {
      users = deserialize();
      User targetUser = findUserByName(user);
      if (targetUser != null) {
        targetUser.addFriend(new Friend(friend));
      }
      serialize();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public List<User> getAllUsers() {
    users = deserialize();
    return users;
  }
  
  // remove friend
  
}
