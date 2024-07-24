package com.feastFriends.feastFriends.websockets;

import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.CloseStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feastFriends.feastFriends.model.ResponseMessage;
import com.feastFriends.feastFriends.service.RestaurantService;

import java.util.*;


@Component
public class WebSocketHandler extends TextWebSocketHandler {

  private final Map<WebSocketSession, String> sessionGroupMap = new HashMap<>();
  private final Map<String, List<WebSocketSession>> groupSessionsMap = new HashMap<>();
  private final Map<WebSocketSession, List<String>> requestedGenres = new HashMap<>();
  private final Map<WebSocketSession, List<String>> requestedRestaurants = new HashMap<>();

  @Autowired
  RestaurantService restaurantService = new RestaurantService();

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    // Initialize session data if needed
  }

  @Override
  public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    String payload = message.getPayload();
    // Assume payload is a JSON string with action and groupName or message
    // For example: {"action": "join", "content": "Maddux's Group"}
    //              {"action": "message", "content": "Hello, group!"}
    //              {"action": "addGenre", "content": "ITALIAN"}
    //              {"action": "addGenre", "content": "AMERICAN"}
    //              {"action": "addGenre", "content": "JAPANESE"}
    //              {"action": "getRequestedGenres", "content": "Maddux's Group"}
    //              {"action": "getRequestedRestaurants", "content": "Maddux's Group"}
    //              {"action": "getMatches", "content": "Maddux's Group"}

    Map<String, String> data = parsePayload(payload);
    String action = data.get("action");
    String content = data.get("content");

    switch (action) {
      case "join":
        joinGroup(session, content); // content = groupName
        break;
      // case "message":
      //   broadcastMessage(session, "message", content);
      //   break;
      case "addGenre":
        addRequestedGenre(session, content); // content = genre
        break;
      case "addRestaurant":
        addRequestedRestaurant(session, content); // content = restaurant name
        break;
      case "getRequestedGenres":
        sendMessage(session, "genres", getRequestedGenresForGroup(content)); // content = groupName
        break;
      case "getGenreMatches":
        List<String> genreRequests = getRequestedGenresForGroup(content); // content = groupName
        sendMessage(session, "genres", getMatches(content, genreRequests));
        break;
      case "getRestaurantMatches":
        List<String> genreMatches = getMatches(content, getRequestedGenresForGroup(content)); // content = groupName
        List<String> restaurants = restaurantService.getRestaurantsWithRequestedGenre(genreMatches);
        sendMessage(session, "genres", getMatches(content, restaurants));
        break;
      case "getRequestedRestaurants":
        List<String> genres = getRequestedGenresForGroup(content); // content = groupName
        sendMessage(session, "restaurants", restaurantService.getRestaurantsWithRequestedGenre(genres));
        break;
      default:
        break;
    }
  }

  private Map<String, String> parsePayload(String payload) {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      Map<String, String> map = objectMapper.readValue(payload, new TypeReference<Map<String,String>>(){});
      return map;
    } catch (Exception e) {
      e.printStackTrace();
    }
    // Implement JSON parsing logic here
    // Return a map of key-value pairs from the JSON payload
    return new HashMap<>();
  }

  public void joinGroup(WebSocketSession session, String groupName) {
    if (sessionGroupMap.containsKey(session)) {
      String oldGroup = sessionGroupMap.get(session);
      if (!oldGroup.equals(groupName)) {
        groupSessionsMap.get(oldGroup).remove(session);
        if (groupSessionsMap.get(oldGroup).isEmpty()) {
          groupSessionsMap.remove(oldGroup);
        }
      }
    }
    sessionGroupMap.put(session, groupName);
    groupSessionsMap.computeIfAbsent(groupName, k -> new ArrayList<>()).add(session);
    System.out.println("Session " + session.getId() + " joined group " + groupName); // Log group join
  }

  // private void broadcastMessage(WebSocketSession senderSession, String contentType, List<String> message) {
  //   String groupName = sessionGroupMap.get(senderSession);
  //   List<WebSocketSession> groupSessions = groupSessionsMap.get(groupName);
  //
  //   if (groupSessions != null) {
  //     for (WebSocketSession session : groupSessions) {
  //       if (session.isOpen() && session != senderSession) {
  //         sendMessage(session, contentType, message);
  //       }
  //     }
  //   }
  // }

  private void addRequestedGenre(WebSocketSession session, String genre) {
    requestedGenres.computeIfAbsent(session, k -> new ArrayList<>()).add(genre);
  }

  private void addRequestedRestaurant(WebSocketSession session, String restaurant) {
    requestedRestaurants.computeIfAbsent(session, k -> new ArrayList<>()).add(restaurant);
  }

  private void sendMessage(WebSocketSession session, String contentType, List<String> message) {
    try {
      ResponseMessage responseMessage = new ResponseMessage(contentType, message);
      ObjectMapper objectMapper = new ObjectMapper();
      String jsonString = objectMapper.writeValueAsString(responseMessage);
      session.sendMessage(new TextMessage(jsonString));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    super.afterConnectionClosed(session, status);

    String groupName = sessionGroupMap.get(session);
    if (groupName != null) {
      groupSessionsMap.get(groupName).remove(session);
      if (groupSessionsMap.get(groupName).isEmpty()) {
        groupSessionsMap.remove(groupName);
      }
    }

    sessionGroupMap.remove(session);
    requestedGenres.remove(session);
    requestedRestaurants.remove(session);
  }

  //get requested genres for a specific session
  public List<String> getRequestedGenres(WebSocketSession session) {
    return requestedGenres.getOrDefault(session, Collections.emptyList());
  }

  public List<String> getRequestedRestaurants(WebSocketSession session) {
    return requestedRestaurants.getOrDefault(session, Collections.emptyList());
  }

  public List<String> getRequestedGenresForGroup(String groupName) {
    List<String> genres = new ArrayList<>();
    List<WebSocketSession> sessions = groupSessionsMap.get(groupName);
    if (sessions != null) {
      for (WebSocketSession session : sessions) {
        genres.addAll(requestedGenres.getOrDefault(session, Collections.emptyList()));
      }
    }
    return genres;
  }

  public List<String> getRequestedRestaurantsForGroup(String groupName) {
    List<String> restaurants = new ArrayList<>();
    List<WebSocketSession> sessions = groupSessionsMap.get(groupName);
    if (sessions != null) {
      for (WebSocketSession session : sessions) {
        restaurants.addAll(requestedRestaurants.getOrDefault(session, Collections.emptyList()));
      }
    }
    return restaurants;
  }

  public List<String> getMatches(String groupName, List<String> requests) {
    int groupLength = groupSessionsMap.get(groupName).size();
    return restaurantService.getMatches(requests, groupLength);
  }
}
