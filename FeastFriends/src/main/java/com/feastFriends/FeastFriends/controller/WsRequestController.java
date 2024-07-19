package com.feastFriends.feastFriends.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.http.ResponseEntity;

import com.feastFriends.feastFriends.websockets.*;

import java.util.List;

@RestController
@RequestMapping("/api/ws")
public class WsRequestController {

  @Autowired
  private WebSocketHandler webSocketHandler;

  @GetMapping("/getSessionMessages")
  public ResponseEntity<List<String>> getSessionMessages(@RequestBody String ownerName) {
    WebSocketSession session = findSessionById(ownerName.trim()+"'s Group"); // Implement a method to find session by ID
    if (session != null) {
      List<String> messages = webSocketHandler.getMessagesForSession(session);
      return ResponseEntity.ok(messages);
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @PostMapping("/request_genre")
  public ResponseEntity<String> requestGenre(String genre) {
    return ResponseEntity.ok("Successfully requested genre");
  }

  private WebSocketSession findSessionById(String sessionId) {
    // Implement logic to retrieve WebSocketSession by its ID or other identifier
    return null;
  }
}
