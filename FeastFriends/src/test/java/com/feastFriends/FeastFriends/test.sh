#!/bin/bash

{
  sleep 1
  printf "{\"action\": \"name\", \"content\": \"Maddux\"}\n"
  sleep 1
  printf "{\"action\": \"join\", \"content\": \"Maddux's Group\"}\n"
  sleep 1
  printf "{\"action\": \"addGenre\", \"content\": \"ITALIAN\"}\n"
  sleep 1
  printf "{\"action\": \"addGenre\", \"content\": \"AMERICAN\"}\n"
  sleep 1
  printf "{\"action\": \"addGenre\", \"content\": \"JAPANESE\"}\n"
  sleep 1
  printf "{\"action\": \"getRequestedGenres\", \"content\": \"Maddux's Group\"}\n"
  sleep 1
  printf "{\"action\": \"done\", \"content\": \"Maddux's Group\"}\n"
  sleep 1
  printf "{\"action\": \"getGenreMatches\", \"content\": \"Maddux's Group\"}\n"
  sleep 1
  printf "{\"action\": \"getRestaurantChoices\", \"content\": \"Maddux's Group\"}\n"
  sleep 1
  printf "{\"action\": \"addRestaurant\", \"content\": \"Burger King\"}\n"
  sleep 1
  printf "{\"action\": \"addRestaurant\", \"content\": \"Ichiban\"}\n"
  sleep 1
  printf "{\"action\": \"getRequestedRestaurants\", \"content\": \"Maddux's Group\"}\n"
  sleep 1
  printf "{\"action\": \"done\", \"content\": \"Maddux's Group\"}\n"
  sleep 1
  printf "{\"action\": \"getRestaurantMatches\", \"content\": \"Maddux's Group\"}\n"
} | websocat ws://localhost:8080/ws
