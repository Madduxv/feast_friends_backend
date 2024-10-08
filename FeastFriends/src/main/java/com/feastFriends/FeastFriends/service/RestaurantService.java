package com.feastFriends.feastFriends.service;

import com.feastFriends.feastFriends.model.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.stream.Collectors;

@Service
public class RestaurantService {
  
  private List<Restaurant> restaurants = new ArrayList<>();
  private List<String> requestedGenres = new ArrayList<>();

  @PostConstruct
  public void init() {
    ObjectMapper mapper = new ObjectMapper();
    TypeReference<List<Restaurant>> typeReference = new TypeReference<List<Restaurant>>() {};
    InputStream inputStream = TypeReference.class.getResourceAsStream("/restaurantData.json");

    try {
      restaurants = mapper.readValue(inputStream, typeReference);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // public void addRestaurant(Restaurant restaurant) {
  //   restaurants.add(restaurant);
  // }

  public List<Restaurant> getAllRestaurants() {
    return restaurants;
  }

  // gotta give the functional bros some love
  public List<String> findRestaurantsWithRequestedGenre() {
    return restaurants.stream()
    .filter(restaurant -> requestedGenres.contains(restaurant.getGenre()))
    .map(Restaurant::getName)
    .collect(Collectors.toList());
  }

  // this one is for groups to use
  public List<String> getRestaurantsWithRequestedGenre(List<String> deezRequestedGenres) {
    return restaurants.stream()
    .filter(restaurant -> deezRequestedGenres.contains(restaurant.getGenre()))
    .map(Restaurant::getName)
    .collect(Collectors.toList());
  }


  public List<String> getMatches(List<String> requests, int groupMembers) {
    List<String> fillerList = new ArrayList<>();

    for(int i = 0; i<groupMembers-1; i++) {
      fillerList.clear();
      Iterator<String> iterator = requests.iterator();

      while (iterator.hasNext()) {
        String element = iterator.next();

        if (!fillerList.contains(element)){
          fillerList.add(element);
          iterator.remove();
        }
      } 
    }

    return requests;
  }

  public void addRequestedGenre(String genre) {
    requestedGenres.add(genre);
  }

  public List<String> getRequestedGenres() {
    return requestedGenres;
  }

  public void clearRequestedGenres() {
    requestedGenres.clear();
  }
}
