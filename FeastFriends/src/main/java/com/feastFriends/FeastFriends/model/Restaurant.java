package com.feastFriends.feastFriends.model;

public class Restaurant {
  private String name;
  private String genre;

  public void addRestaurant(String name, String genre) {
    this.name = name;
    this.genre = genre;
  }

  public String getRestaurant() {
    return name;
  }
}
