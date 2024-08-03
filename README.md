# Feast Friends Backend API Endpoints

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
**Content:**
- content should be your name

**Response**
- the response will be null

#### join
**Content:** 
- content should be the name of the group you want to join

**Response:** 
- the response will be 

#### done
**Content:** 
- content should be the name of your group

**Response:** 
- the response will be null 

#### addGenre
**Content:** 
- content should be the name of the genre you would like to request 

**Response:** 
- the response will be 

#### getRequestedGenres
**Content:** 
- content should be the name of your group 

**Response:** 
- the response will be: {"contentType": "genres": "message": [List, of, genres]} 
- the list of genres will be a list of the genres that your group requested

#### getGenreMatches
**Content:** 
- content should be the name of your group

**Response:** 
- the response will be: {"contentType": "genreMatches": "message": [List, of, genres]} 
- the list of genres will be a list of the genres that your group matched on

#### getRestaurantChoices
**Content:** 
- content should be the name of your group

**Response:** 
- the response will be: {"contentType": "restaurants": "message": [List, of, restaurants]} 
- the list of restaurants will be the restaurants that have any of the restaurants that your group matched on

#### addRestaurant
**Content:** 
- content should be the restaurant you would like to request

**Response:** 
- the response will be null

#### getRequestedRestaurants
**Content:** 
- content should be the name of your group 

**Response:** 
- the response will be: {"contentType": "groupRestaurants": "message": [List, of, restaurants]} 
- the list of genres will be a list of the restaurants that your group requested

#### getRestaurantMatches
**Content:** 
- content should be the name of your group

**Response:** 
- the response will be: {"contentType": "restaurantMatches": "message": [List, of, restaurants]} 
- the list of genres will be a list of the restaurants that your group matched on

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

## User REST Endpoints 

