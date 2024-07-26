package com.feastFriends.feastFriends.websockets;

import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.CloseStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feastFriends.feastFriends.model.ListResponseMessage;
import com.feastFriends.feastFriends.model.StringResponseMessage;
import com.feastFriends.feastFriends.service.RestaurantService;

import java.util.Map;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class WebSocketHandler extends TextWebSocketHandler {

  private final Map<WebSocketSession, String> sessionGroupMap = new ConcurrentHashMap<>();
  private final Map<String, List<WebSocketSession>> groupSessionsMap = new ConcurrentHashMap<>();
  private final Map<WebSocketSession, List<String>> requestedGenres = new ConcurrentHashMap<>();
  private final Map<WebSocketSession, List<String>> requestedRestaurants = new ConcurrentHashMap<>();
  private final Map<String, Integer> groupDoneMap = new ConcurrentHashMap<>();

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
    //              {"action": "addGenre", "content": "ITALIAN"}
    //              {"action": "addGenre", "content": "AMERICAN"}
    //              {"action": "addGenre", "content": "JAPANESE"}
    //              {"action": "getRequestedGenres", "content": "Maddux's Group"}
    //              {"action": "done", "content": "Maddux's Group"}
    //              {"action": "getGenreMatches", "content": "Maddux's Group"}
    //              {"action": "getRestaurantChoices", "content": "Maddux's Group"}
    //              {"action": "addRestaurant", "content": "Burger King"}
    //              {"action": "addRestaurant", "content": "Ichiban"}
    //              {"action": "getRequestedRestaurants", "content": "Maddux's Group"}
    //              {"action": "done", "content": "Maddux's Group"}
    //              {"action": "getRestaurantMatches", "content": "Maddux's Group"}

    Map<String, String> data = parsePayload(payload);
    String action = data.get("action");
    String content = data.get("content");

    switch (action) {
      case "join": // find user page
        joinGroup(session, content); // content = groupName
        break;
      case "done": //genres and restaurants page
        if (addDoneMember(content)) {
          groupDoneMap.put(content, 0);
          broadcastMessage(session, "groupDoneStatus", "Everyone is done");
        }
        break;
      case "addGenre": // genre page
        addRequestedGenre(session, content); // content = genre
        break;
      case "getRequestedGenres": // genre page debug
        sendListMessage(session, "genres", getRequestedGenresForGroup(content)); // content = groupName
        break;
      case "getGenreMatches": // user complete waiting page
        List<String> genreRequests = getRequestedGenresForGroup(content); // content = groupName
        sendListMessage(session, "genreMatches", getMatches(content, genreRequests));
        break;
      case "getRestaurantChoices": // user complete waiting page
        List<String> genres = getRequestedGenresForGroup(content); // content = groupName
        List<String> genreMatches = getMatches(content, genres);
        sendListMessage(session, "restaurants", restaurantService.getRestaurantsWithRequestedGenre(genreMatches));
        break;
      case "addRestaurant": // restaurant page
        addRequestedRestaurant(session, content); // content = restaurant name
        break;
      case "getRequestedRestaurants": // restaurant page debug
        sendListMessage(session, "groupRestaurants", getRequestedRestaurantsForGroup(content)); // content = groupName
        break;
      case "getRestaurantMatches": // results  page
        List<String> restaurants = getRequestedRestaurantsForGroup(content); // content = groupName
        sendListMessage(session, "restaurantMatches", getMatches(content, restaurants));
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
    groupDoneMap.putIfAbsent(groupName, 0);
    System.out.println("Session " + session.getId() + " joined group " + groupName); // Log group join
  }

  private void broadcastMessage(WebSocketSession senderSession, String contentType, String message) {
    String groupName = sessionGroupMap.get(senderSession);
    List<WebSocketSession> groupSessions = groupSessionsMap.get(groupName);

    if (groupSessions != null) {
      for (WebSocketSession session : groupSessions) {
        if (session.isOpen()) {
          sendStringMessage(session, contentType, message);
        }
      }
    }
  }

  private void addRequestedGenre(WebSocketSession session, String genre) {
    requestedGenres.computeIfAbsent(session, k -> new ArrayList<>()).add(genre);
  }

  private void addRequestedRestaurant(WebSocketSession session, String restaurant) {
    requestedRestaurants.computeIfAbsent(session, k -> new ArrayList<>()).add(restaurant);
  }

  private boolean addDoneMember(String groupName) {
    groupDoneMap.compute(groupName, (key, value) -> (value == null) ? 0 : value + 1);
    if (getGroupSize(groupName) == groupDoneMap.get(groupName)) {
      return true;
    }
    return false;
  }

  private Integer getGroupSize(String groupName) {
    return groupSessionsMap.get(groupName) != null ? groupSessionsMap.get(groupName).size() : 0;
  }

  private void sendListMessage(WebSocketSession session, String contentType, List<String> message) {
    try {
      ListResponseMessage listResponseMessage = new ListResponseMessage(contentType, message);
      ObjectMapper objectMapper = new ObjectMapper();
      String jsonString = objectMapper.writeValueAsString(listResponseMessage);
      session.sendMessage(new TextMessage(jsonString));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void sendStringMessage(WebSocketSession session, String contentType, String message) {
    try {
      StringResponseMessage stringResponseMessage = new StringResponseMessage(contentType, message);
      ObjectMapper objectMapper = new ObjectMapper();
      String jsonString = objectMapper.writeValueAsString(stringResponseMessage);
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
        groupDoneMap.remove(groupName);
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
