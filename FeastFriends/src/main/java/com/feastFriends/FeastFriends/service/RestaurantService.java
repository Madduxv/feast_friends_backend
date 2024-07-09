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
  public List<Restaurant> findRestaurantsWithRequestedGenre() {
    List<String> genres = requestedGenres;
    return restaurants.stream()
    .filter(restaurant -> genres.contains(restaurant.getGenre()))
    .collect(Collectors.toList());
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
