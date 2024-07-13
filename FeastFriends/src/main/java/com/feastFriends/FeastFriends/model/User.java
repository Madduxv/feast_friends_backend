package com.feastFriends.feastFriends.model;

import jakarta.persistence.*;
import java.util.Set;
import java.util.HashSet;

@Entity
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long ID;
  private String name;
  @ManyToMany
  @JoinTable(
    name = "user_friends",
    joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns = @JoinColumn(name = "friend_id")
  )
  private Set<User> friends = new HashSet<>();

  public Long getUserID() {
    return ID;
  }

  public String getUserName() {
    return name;
  }

  public Set<User> getUserFriends() {
    return friends; 
  }

  // public void addUserFriend(String friend) {
  //   friends.add(friend);
  // }

  public void setUserFriends(Set<User> friends) {
    this.friends = friends;
  }

  public void setUserName(String name) {
    this.name = name;
  }

  public void setUserID(Long ID) {
    this.ID = ID;
  }

}
