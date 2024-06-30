package com.feastFriends.FeastFriends.model;

import java.util.ArrayList;
import java.util.List;

public class Restaurant {
  private String name;
  private String genre;

  public void setPreferredGenre(String genre) {
    this.genre = genre;
  }
  
  public List getRestaurants() {
    return new ArrayList<String>();
  }

  public void addRestaurant(String name) {
    this.name = name;
  }
}
