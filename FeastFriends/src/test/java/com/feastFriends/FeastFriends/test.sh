#!/bin/bash

{
  sleep 2
  printf "{\"action\": \"name\", \"content\": \"Maddux\"}\n"
  sleep 2
  printf "{\"action\": \"join\", \"content\": \"Maddux's Group\"}\n"
  sleep 2
  printf "{\"action\": \"addGenre\", \"content\": \"ITALIAN\"}\n"
  sleep 2
  printf "{\"action\": \"addGenre\", \"content\": \"AMERICAN\"}\n"
  sleep 2
  printf "{\"action\": \"addGenre\", \"content\": \"JAPANESE\"}\n"
  sleep 2
  printf "{\"action\": \"getRequestedGenres\", \"content\": \"Maddux's Group\"}\n"
  sleep 2
  printf "{\"action\": \"addRestaurant\", \"content\": \"Burger King\"}\n"
  sleep 2
  printf "{\"action\": \"addRestaurant\", \"content\": \"Ichiban\"}\n"
  sleep 2
  printf "{\"action\": \"addRestaurant\", \"content\": \"Texas Roadhouse\"}\n"
  sleep 2
  printf "{\"action\": \"getRequestedRestaurants\", \"content\": \"Maddux's Group\"}\n"
  sleep 5
} | websocat ws://localhost:8080/ws
