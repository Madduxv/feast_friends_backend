package com.feastFriends.feastFriends.controller;

// import com.feastFriends.feastFriends.model;
import com.feastFriends.feastFriends.service.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cards")
public class RestaurantController {

  private RestaurantService restaurantService;

  @PostMapping("/request_genre")
  public void requestGenre(@RequestBody String genre) {
    restaurantService.setRequestedGenre(genre);
  }

  @GetMapping("/requested_restaurants")
  public String getGenre() {
    return restaurantService.getRequestedGenre();
  }

}
