package com.feastFriends.feastFriends.websockets;

import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.CloseStatus;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;


@Component
public class WebSocketHandler extends TextWebSocketHandler {

  private final Map<WebSocketSession, String> sessionGroupMap = new HashMap<>();
  private final Map<String, List<WebSocketSession>> groupSessionsMap = new HashMap<>();
  private final Map<WebSocketSession, List<String>> requestedGenres = new HashMap<>();

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    // Initialize session data if needed
  }

  @Override
  public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    String payload = message.getPayload();
    // Assume payload is a JSON string with action and groupName or message
    // For example: {"action": "join", "content": "exampleGroup"}
    //              {"action": "message", "content": "Hello, group!"}
    //              {"action": "addGenre", "content": "ITALIAN"}
    //              {"action": "addGenre", "content": "AMERICAN"}
    //              {"action": "addGenre", "content": "JAPANESE"}
    //              {"action": "getRequestedGenres", "content": "exampleGroup"}

    Map<String, String> data = parsePayload(payload);
    String action = data.get("action");

    if ("join".equals(action)) {
      String groupName = data.get("content");
      joinGroup(session, groupName);
    } else if ("message".equals(action)) {
      String content = data.get("content");
      broadcastMessage(session, content);
    } else if ("addGenre".equals(action)) {
      String genre = data.get("content");
      addRequestedGenre(session, genre);
    } else if ("getRequestedGenres".equals(action)) {
      String groupName = data.get("content");
      List <String> genres = getRequestedGenresForGroup(groupName);
      sendMessage(session, genres.toString());
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

  private void joinGroup(WebSocketSession session, String groupName) {
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
    sendMessage(session, "Joined group: " + groupName);
  }

  private void broadcastMessage(WebSocketSession senderSession, String message) {
    String groupName = sessionGroupMap.get(senderSession);
    List<WebSocketSession> groupSessions = groupSessionsMap.get(groupName);

    if (groupSessions != null) {
      for (WebSocketSession session : groupSessions) {
        if (session.isOpen() && session != senderSession) {
          sendMessage(session, message);
        }
      }
    }
  }

  private void addRequestedGenre(WebSocketSession session, String genre) {
    requestedGenres.computeIfAbsent(session, k -> new ArrayList<>()).add(genre);
  }

  private void sendMessage(WebSocketSession session, String message) {
    try {
      session.sendMessage(new TextMessage(message));
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
  }

  //get requested genres for a specific session
  public List<String> getRequestedGenres(WebSocketSession session) {
    return requestedGenres.getOrDefault(session, Collections.emptyList());
  }

  public List<String> getRequestedGenresForGroup(String groupName) {
    List<String> genresForGroup = new ArrayList<>();
    List<WebSocketSession> sessions = groupSessionsMap.get(groupName);

    if (sessions != null) {
      for (WebSocketSession session : sessions) {
        List<String> genres = requestedGenres.get(session);
        if (genres != null) {
          genresForGroup.addAll(genres);
        }
      }
    }
    return genresForGroup;
  }
}
