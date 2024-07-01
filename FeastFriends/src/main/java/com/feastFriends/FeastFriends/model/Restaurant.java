package com.feastFriends.feastFriends.model;

public class Restaurant {
  private String name;
  private String genre;

  // public addRestaurantModel(String name, String genre) {
  //   this.name = name;
  //   this.genre = genre;
  // }

  public void setPreferredGenre(String genre) {
    this.genre = genre;
  }
  
  public String getRestaurant() {
    return name;
  }
}
