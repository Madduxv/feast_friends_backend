package com.feastFriends.feastFriends.model;


public class Friend {

  private Long id;
  private String name;

  public Friend() {}

  public Friend(Long id, String name) {
    this.id = id;
    this.name = name;
  }

  public Long getID() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setID(Long id) {
    this.id = id;
  }

}
