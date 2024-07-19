// package com.feastFriends.feastFriends.controller;
//
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.web.bind.annotation.*;
// import org.springframework.web.socket.WebSocketSession;
// import org.springframework.http.ResponseEntity;
//
// import com.feastFriends.feastFriends.websockets.*;
// import com.feastFriends.feastFriends.service.RestaurantService;
// import com.feastFriends.feastFriends.service.GroupService;
//
// import java.util.List;
//
// @RestController
// @RequestMapping("/api/ws")
// public class WsRequestController {
//
//   private GroupService groupService;
//   private WebSocketHandler webSocketHandler;
//
//   @Autowired
//   public WsRequestController(GroupService groupService, WebSocketHandler webSocketHandler) {
//     this.groupService = groupService;
//     this.webSocketHandler = webSocketHandler;
//   }
//
//   @PostMapping("/join")
//   public ResponseEntity<String> joinGroup(@RequestParam String sessionId, @RequestParam String groupName) {
//     WebSocketSession session = webSocketHandler.getSessionById(sessionId);
//     if (session != null) {
//       webSocketHandler.addSessionToGroup(session, groupName);
//       return ResponseEntity.ok("Joined group: " + groupName);
//     }
//     return ResponseEntity.badRequest().body("Session not found");
//   }
//
//   @PostMapping("/addGenre")
//   public ResponseEntity<String> addRequestedGenre(@RequestParam String groupName, @RequestParam String genre) {
//     groupService.addRequestedGenre(groupName, genre);
//     return ResponseEntity.ok("Genre added to group " + groupName + ": " + genre);
//   }
//
//   @GetMapping("/requestedGenres")
//   public ResponseEntity<List<String>> getRequestedGenres(@RequestParam String groupName) {
//     List<String> genres = groupService.getRequestedGenres(groupName);
//     return ResponseEntity.ok(genres);
//   }
// }
