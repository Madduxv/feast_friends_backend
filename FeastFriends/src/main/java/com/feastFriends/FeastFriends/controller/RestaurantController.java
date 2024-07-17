package com.feastFriends.feastFriends.controller;
// import com.feastFriends.feastFriends.model;
import com.feastFriends.feastFriends.service.*;

import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurant")
public class RestaurantController {

  @Autowired
  private RestaurantService restaurantService;

  @PostMapping("/request_genre")
  public ResponseEntity<String> requestGenre(@RequestBody String genre) {
    restaurantService.addRequestedGenre(genre);
    return ResponseEntity.ok("Requesting " + genre);
  }
  @PostMapping("/clear_genres")
  public ResponseEntity<String> clearRequestedGenres() {
    restaurantService.clearRequestedGenres();
    return ResponseEntity.ok("Cleared requested Genres");
  }
  
  @GetMapping("/requested_genres")
  public ResponseEntity<List<String>> getGenre() {
    return ResponseEntity.ok(restaurantService.getRequestedGenres());
  }
  @GetMapping("/requested_restaurants")
  public ResponseEntity<List> getRequestedRestaurants() {
    return ResponseEntity.ok(restaurantService.findRestaurantsWithRequestedGenre());
  }
  @GetMapping("/all_restaurants")
  public ResponseEntity<List> getAllRestaurants() {
    return ResponseEntity.ok(restaurantService.getAllRestaurants());
  }

}
