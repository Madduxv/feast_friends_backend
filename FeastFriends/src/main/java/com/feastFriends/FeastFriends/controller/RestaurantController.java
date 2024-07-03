package com.feastFriends.feastFriends.controller;

// import com.feastFriends.feastFriends.model;
import com.feastFriends.feastFriends.service.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RestaurantController {

  @Autowired
  private RestaurantService restaurantService;

  @PostMapping("/request_genre")
  public String requestGenre(@RequestBody String genre) {
    restaurantService.addRequestedGenre(genre);
    return genre;
  }

  @GetMapping("/requested_genre")
  public List getGenre() {
    return restaurantService.getRequestedGenres();
  }
  @GetMapping("/requested_restaurants")
  public List getRequestedRestaurants() {
    return restaurantService.findRestaurantsWithRequestedGenre();
  }
  @GetMapping("/all_restaurants")
  public List getAllRestaurants() {
    return restaurantService.getAllRestaurants();
  }
  @GetMapping("/clear_genres")
  public String clearRequestedGenres() {
    restaurantService.clearRequestedGenres();
    return "Cleared requested Genres";
  }
}
