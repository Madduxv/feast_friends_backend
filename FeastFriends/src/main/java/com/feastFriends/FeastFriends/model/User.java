package com.feastFriends.feastFriends.model;

import java.util.List;
import java.util.ArrayList;

public class User {

  private String name;
  private List<Friend> friends = new ArrayList<>();

  public User() {}

  public User(String name, List<Friend> friends) {
    this.name = name;
    this.friends = friends;
  }

  public String getName() {
    return name;
  }

  public List<Friend> getFriends() {
    return friends; 
  }
  
  public void setFriends(List<Friend> friends) {
    this.friends = friends;
  }

  public void addFriend(Friend friend) {
    this.friends.add(friend);
  }

  public void setName(String name) {
    this.name = name;
  }

}
