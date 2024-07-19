// package com.feastFriends.feastFriends.service;
//
// import org.springframework.stereotype.Service;
// import org.springframework.web.socket.WebSocketSession;
//
// import java.util.*;
//
// @Service
// public class GroupService {
//
//   private final Map<String, List<WebSocketSession>> groupSessionsMap = new HashMap<>();
//   private final Map<String, List<String>> groupRequestedGenres = new HashMap<>();
//
//   public void addSessionToGroup(WebSocketSession session, String groupName) {
//     groupSessionsMap.computeIfAbsent(groupName, k -> new ArrayList<>()).add(session);
//   }
//
//   public void removeSessionFromGroup(WebSocketSession session, String groupName) {
//     List<WebSocketSession> sessions = groupSessionsMap.get(groupName);
//     if (sessions != null) {
//       sessions.remove(session);
//       if (sessions.isEmpty()) {
//         groupSessionsMap.remove(groupName);
//         groupRequestedGenres.remove(groupName);
//       }
//     }
//   }
//
//   public void addRequestedGenre(String groupName, String genre) {
//     groupRequestedGenres.computeIfAbsent(groupName, k -> new ArrayList<>()).add(genre);
//   }
//
//   public List<String> getRequestedGenres(String groupName) {
//     return groupRequestedGenres.getOrDefault(groupName, Collections.emptyList());
//   }
// }
