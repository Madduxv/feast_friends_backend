<div align="center">
# Feast Friends Backend API Endpoints
</div>

## ws://localhost:8080/ws

### Message Structure
**Messages sent to this websocket should be structured as follows:**
- {"action": "yourAction", "content": "yourContent"}

**Possible actions:**
- name
- join
- done
- addGenre
- getRequestedGenres
- getGenreMatches
- getRestaurantChoices
- addRestaurant
- getRequestedRestaurants
- getRestaurantMatches

### Usage and responses:

#### name
- content should be your name
- the response will be null

#### join
- content should be the name of the group you want to join
- the response will be 

#### done
- content should be the name of your group
- the response will be null 

#### addGenre
- content should be the name of the genre you would like to request 
- the response will be 

#### getRequestedGenres
- content should be the name of your group 
- the response will be: {"contentType": "genres": "message": [List, of, genres]} 
`the list of genres will be a list of the genres that your group requested`

#### getGenreMatches
- content should be the name of your group
- the response will be: {"contentType": "genreMatches": "message": [List, of, genres]} 
`the list of genres will be a list of the genres that your group matched on`

#### getRestaurantChoices
- content should be the name of your group
- the response will be: {"contentType": "restaurants": "message": [List, of, restaurants]} 
`the list of restaurants will be the restaurants that have any of the restaurants that your group matched on`

#### addRestaurant
- content should be the restaurant you would like to request
- the response will be null

#### getRequestedRestaurants
- content should be the name of your group 
- the response will be: {"contentType": "groupRestaurants": "message": [List, of, restaurants]} 
`the list of genres will be a list of the restaurants that your group requested`

#### getRestaurantMatches
- content should be the name of your group
- the response will be: {"contentType": "restaurantMatches": "message": [List, of, restaurants]} 
`the list of genres will be a list of the restaurants that your group matched on`

### Example workflow
1. {"action": "name", "content": "Maddux"}
2. {"action": "join", "content": "Maddux's Group"}
3. {"action": "addGenre", "content": "ITALIAN"}
4. {"action": "addGenre", "content": "AMERICAN"}
5. {"action": "addGenre", "content": "JAPANESE"}
6. {"action": "getRequestedGenres", "content": "Maddux's Group"}
7. {"action": "done", "content": "Maddux's Group"}
8. {"action": "getGenreMatches", "content": "Maddux's Group"}
9. {"action": "getRestaurantChoices", "content": "Maddux's Group"}
10. {"action": "addRestaurant", "content": "Burger King"}
11. {"action": "addRestaurant", "content": "Ichiban"}
12. {"action": "getRequestedRestaurants", "content": "Maddux's Group"}
13. {"action": "done", "content": "Maddux's Group"}
14. {"action": "getRestaurantMatches", "content": "Maddux's Group"}

## Restaurant REST Endpoints 

### http://localhost:8080/api/restaurant/all_restaurants
- 
``` 
# To use this endpoint, you can use curl:
curl http://localhost:8080/api/restaurant/all_restaurants
```
- This endpoint returns a JSON response with all restaurants

### These endpoints are deprecated
- http://localhost:8080/api/restaurant/request_genre
- http://localhost:8080/api/restaurant/clear_genres
- http://localhost:8080/api/restaurant/requested_genres
- http://localhost:8080/api/restaurant/requested_restaurants


## User REST Endpoints 

### http://localhost:8080/api/user/find_user
- This endpoint takes a String RequestBody of the user you want to find
``` 
# For example:
curl -X GET localhost:8080/api/user/find_user -d "Maddux"
```
- The server will return a JSON response with the user's name and friends.
``` 
# Example response:
{"name":"Maddux","friends":[{"name":"Trin"},{"name":"Cassie"},{"name":"Alaura"}]}
```

### http://localhost:8080/api/user/get_friends
- This endpoint takes a String RequestBody of the user whose friends you want to find.
``` 
# For example:
curl -G localhost:8080/api/user/get_friends --data-urlencode "name=Maddux"
```
- The server will return a JSON response with the user's name and friends.
``` 
# Example response:
[{"name":"Trin"},{"name":"Cassie"},{"name":"Alaura"}]
```

### http://localhost:8080/api/user/all_users
- This endpoint takes no parameters
``` 
# To interact with curl:
curl -X GET localhost:8080/api/user/all_users
```
- The server will respond with a JSON list of all users

### http://localhost:8080/api/user/all_users_names
- This endpoint takes no parameters
```
# To interact with curl:
curl -X GET localhost:8080/api/user/all_users_names
```
- The server will respond with a JSON list of all users names

### http://localhost:8080/api/user/add_user
```
# To interact with curl:
curl -X POST localhost:8080/api/user/add_user -d "Kai"
```
- The server will respond with "Added User"

### http://localhost:8080/api/user/add_friend
- This endpoint takes 2 parameters: "url?user=yourName&friend=friendName"
``` 
# For example:
curl -X POST "http://localhost:8080/api/user/add_friend" -d "user=Kai" -d "friend=Maddux"
```
