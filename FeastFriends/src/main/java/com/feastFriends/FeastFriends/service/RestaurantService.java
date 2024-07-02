package com.feastFriends.feastFriends.service;

import java.util.ArrayList;
import java.util.List;

import com.feastFriends.feastFriends.model.*;
import org.springframework.stereotype.Service;

@Service
public class RestaurantService {
  
  private List<Restaurant> restaurants = new ArrayList<>();
  private List<String> requestedGenres = new ArrayList<>();

  // public void addRestaurant(Restaurant restaurant) {
  //   restaurants.add(restaurant);
  // }
  //
  // public List<Restaurant> getRestaurants() {
  //   return restaurants;
  // }
  
  public void addRequestedGenre(String genre) {
    requestedGenres.add(genre);
  }

  public List getRequestedGenres() {
    return requestedGenres;
  }
}
