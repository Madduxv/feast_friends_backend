package com.feastFriends.feastFriends.service;

import java.util.ArrayList;
import java.util.List;

import com.feastFriends.feastFriends.model.*;
import org.springframework.stereotype.Service;

@Service
public class RestaurantService {
  
  private List<Restaurant> restaurants = new ArrayList<>();
  private String requestedGenre = new String(""); 

  public void addRestaurant(Restaurant restaurant) {
    restaurants.add(restaurant);
  }

  public List<Restaurant> getRestaurants() {
    return restaurants;
  }
  
  public void setRequestedGenre(String genre) {
    requestedGenre = genre;
  }

  public String getRequestedGenre() {
    return requestedGenre;
  }
}
