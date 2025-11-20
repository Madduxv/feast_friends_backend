package com.feastFriends.feastFriends.websockets;

import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.CloseStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feastFriends.feastFriends.model.ListResponseMessage;
import com.feastFriends.feastFriends.model.StringResponseMessage;
import com.feastFriends.feastFriends.model.Friend;
import com.feastFriends.feastFriends.service.RestaurantService;
import com.feastFriends.feastFriends.service.UserService;
import com.feastFriends.feastFriends.service.RedisService;

import java.util.Map;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.annotation.PostConstruct;
import java.util.stream.Collectors;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

  private Map<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

  // Yes I know redis would wouk better. I will implement it later.
  // (later has arrived)

  @Autowired
  RestaurantService restaurantService = new RestaurantService();

  @Autowired
  private UserService userService;

  private RedisService redisService;

  @Value("${redis.host}")
  private String redisHost;

  @Value("${redis.port}")
  private int redisPort;

  @PostConstruct
  public void init() {
    try {
      redisService = new RedisService(redisHost, redisPort);
      // Further initialization if needed
    } catch (Exception e) {
      // Handle exception, log the error, or take corrective actions
      e.printStackTrace();
      System.err.println("Failed to initialize RedisService: " + e.getMessage());
    }
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    // Initialize session data if needed
    sessionMap.put(session.getId(), session);
  }

  @Override
  public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    String payload = message.getPayload();
    // Assume payload is a JSON string with action and groupName or message
    // For example:

    // {"action": "name", "content": "Maddux"}
    // {"action": "join", "content": "Maddux's Group"}
    // {"action": "addGenre", "content": "ITALIAN"}
    // {"action": "addGenre", "content": "AMERICAN"}
    // {"action": "addGenre", "content": "JAPANESE"}
    // {"action": "getRequestedGenres", "content": "Maddux's Group"}
    // {"action": "done", "content": "Maddux's Group"}
    // {"action": "getGenreMatches", "content": "Maddux's Group"}
    // {"action": "getRestaurantChoices", "content": "Maddux's Group"}
    // {"action": "addRestaurant", "content": "Burger King"}
    // {"action": "addRestaurant", "content": "Ichiban"}
    // {"action": "getRequestedRestaurants", "content": "Maddux's Group"}
    // {"action": "done", "content": "Maddux's Group"}
    // {"action": "getRestaurantMatches", "content": "Maddux's Group"}

    Map<String, String> data = parsePayload(payload);
    String action = data.get("action");
    String content = data.get("content");

    switch (action) {
      // {"action": "test", "content": ""}
      // case "test":
      // redisService.testRedis();
      // break;

      case "join": // find user page
        joinGroup(session, content); // content = groupName
        break;

      case "name": // find user page
        addSessionName(session, content); // content = name
        break;

      case "friendsGroups": // find user page
        getUserActiveFriendsGroups(session);
        break;

      case "done": // genres and restaurants page
        addDoneMember(content).thenAccept(groupDone -> {
          if (groupDone) {
            broadcastMessageToGroup(session, "groupDoneStatus", "Everyone is done");
          }
        });
        break;

      case "addGenre": // genre page
        addRequestedGenre(session, content); // content = genre
        break;

      case "getRequestedGenres": // content = groupName
        redisService.addCommandToQueue(redisService.sendKCommand("SGET", content));
        redisService.addCommandToQueue(() -> CompletableFuture.supplyAsync(() -> {
          String response = redisService.getLastResult();
          List<String> genres = response == null || response.trim().isEmpty()
              ? new ArrayList<>()
              : Arrays.stream(response.split(","))
                  .map(String::trim)
                  .filter(s -> !s.isEmpty())
                  .collect(Collectors.toList());

          sendListMessage(session, "genres", genres);
          return "";
        }));
        break;

      case "getGenreMatches": // user complete waiting page
        redisService.addCommandToQueue(redisService.sendKCommand("SGET", content));
        redisService.addCommandToQueue(() -> CompletableFuture.supplyAsync(() -> {
          String response = redisService.getLastResult();
          List<String> genres = response == null || response.trim().isEmpty()
              ? new ArrayList<>()
              : Arrays.stream(response.split(","))
                  .map(String::trim)
                  .filter(s -> !s.isEmpty())
                  .collect(Collectors.toList());

          // Chain the async getMatches
          getMatches(content, genres).thenAccept(matches -> {
            sendListMessage(session, "genreMatches", matches);
          });

          return ""; // placeholder for the queue
        }));
        break;

      case "getRestaurantChoices": // user complete waiting page
        redisService.addCommandToQueue(redisService.sendKCommand("SGET", content));
        redisService.addCommandToQueue(() -> CompletableFuture.supplyAsync(() -> {
          String response = redisService.getLastResult();
          List<String> genres = response == null || response.trim().isEmpty()
              ? new ArrayList<>()
              : Arrays.stream(response.split(","))
                  .map(String::trim)
                  .filter(s -> !s.isEmpty())
                  .collect(Collectors.toList());

          // Chain async getMatches and then restaurant filtering
          getMatches(content, genres).thenAccept(genreMatches -> {
            List<String> restaurants = restaurantService.getRestaurantsWithRequestedGenre(genreMatches);
            sendListMessage(session, "restaurants", restaurants);
          });

          return ""; // placeholder
        }));
        break;

      case "addRestaurant": // restaurant page
        addRequestedRestaurant(session, content); // content = restaurant name
        break;

      case "getRequestedRestaurants":
        redisService.addCommandToQueue(redisService.sendKCommand("SGET", content));
        redisService.addCommandToQueue(() -> CompletableFuture.supplyAsync(() -> {
          String response = redisService.getLastResult();
          List<String> restaurants = response == null || response.trim().isEmpty()
              ? new ArrayList<>()
              : Arrays.stream(response.split(","))
                  .map(String::trim)
                  .filter(s -> !s.isEmpty())
                  .collect(Collectors.toList());

          sendListMessage(session, "groupRestaurants", restaurants);
          return "";
        }));
        break;

      case "getRestaurantMatches":
        redisService.addCommandToQueue(redisService.sendKCommand("SGET", content));
        redisService.addCommandToQueue(() -> CompletableFuture.supplyAsync(() -> {
          String response = redisService.getLastResult();
          List<String> restaurants = response == null || response.trim().isEmpty()
              ? new ArrayList<>()
              : Arrays.stream(response.split(","))
                  .map(String::trim)
                  .filter(s -> !s.isEmpty())
                  .collect(Collectors.toList());

          // Chain async getMatches
          getMatches(content, restaurants).thenAccept(matches -> {
            sendListMessage(session, "restaurantMatches", matches);
          });

          return ""; // placeholder for queue
        }));
        break;

      default:
        break;
    }
  }

  private Map<String, String> parsePayload(String payload) {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      Map<String, String> map = objectMapper.readValue(payload, new TypeReference<Map<String, String>>() {
      });
      return map;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new HashMap<>();
  }

  public void joinGroup(WebSocketSession session, String groupName) {
    String sessionId = session.getId();

    // Step 1: Get session name
    redisService.addCommandToQueue(() -> redisService.sendKVCommand("HGET", sessionId, "name").get()
        .thenApply(name -> {
          if (name == null || name.trim().isEmpty()) {
            System.out.println("Error getting session's name");
            return null;
          }
          return name.trim();
        }));

    // Step 2: Add user to the new group
    redisService.addCommandToQueue(() -> redisService.sendKVCommand("SADD", groupName, sessionId).get());

    // Step 3: Get old group
    redisService.addCommandToQueue(() -> redisService.sendKVCommand("HGET", sessionId, "group").get()
        .thenApply(oldGroup -> oldGroup != null ? oldGroup.trim() : null));

    // Step 4: Remove from old group if necessary
    redisService.addCommandToQueue(() -> {
      String oldGroup = redisService.getLastResult(); // assume getLastResult() gives the previous HGET response
      if (oldGroup != null && !oldGroup.isEmpty() && !oldGroup.equals(groupName)) {
        return redisService.sendKVCommand("SREM", oldGroup, sessionId).get();
      } else {
        return CompletableFuture.completedFuture("skipped");
      }
    });

    // Step 5: Set new group for the session
    redisService.addCommandToQueue(() -> redisService.sendKFVCommand("HSET", sessionId, "group", groupName).get());

    // Step 6: Log success
    redisService.addCommandToQueue(() -> {
      System.out.printf("Session %s joined %s\n", sessionId, groupName);
      return CompletableFuture.completedFuture("done");
    });
  }

  private void broadcastMessageToGroup(WebSocketSession senderSession, String contentType, String message) {
    String senderId = senderSession.getId();

    // Step 1: queue fetching the sender's group
    redisService.addCommandToQueue(redisService.sendKCommand("HGET", senderId));

    // Step 2: process the group name and queue fetching members
    redisService.addCommandToQueue(() -> CompletableFuture.supplyAsync(() -> {
      String groupName = redisService.getLastResult();
      if (groupName == null || groupName.trim().isEmpty()) {
        return ""; // nothing to broadcast
      }

      redisService.addCommandToQueue(redisService.sendKCommand("SGET", groupName.trim()));
      return "";
    }));

    // Step 3: process group members and queue sending messages
    redisService.addCommandToQueue(() -> CompletableFuture.supplyAsync(() -> {
      String membersCsv = redisService.getLastResult();
      if (membersCsv == null || membersCsv.trim().isEmpty()) {
        return ""; // no members
      }

      String[] usernames = membersCsv.split(",");
      for (String username : usernames) {
        String user = username.trim();
        if (user.isEmpty())
          continue;

        // queue resolving username -> sessionId
        redisService.addCommandToQueue(() -> CompletableFuture.supplyAsync(() -> {
          String sessionId = redisService.getLastResult(); // implement getLastResultForKey() if needed
          if (sessionId != null && !sessionId.trim().isEmpty()) {
            WebSocketSession s = sessionMap.get(sessionId.trim());
            if (s != null && s.isOpen()) {
              sendStringMessage(s, contentType, message);
            }
          }
          return "";
        }));
      }

      return "";
    }));
  }

  private void addRequestedGenre(WebSocketSession session, String genre) {
    String sessionId = session.getId();

    redisService.addCommandToQueue(redisService.sendKFVCommand("RPUSH", sessionId, "genres", genre));
  }

  private void addSessionName(WebSocketSession session, String name) {
    String sessionId = session.getId();

    // Queue the HSET command
    redisService.addCommandToQueue(redisService.sendKFVCommand("HSET", sessionId, "name", name));

    // Queue the SET command (reverse mapping)
    redisService.addCommandToQueue(redisService.sendKVCommand("SET", name, sessionId));

    // Queue a final task to notify WebSocket client once both commands have run
    redisService.addCommandToQueue(() -> CompletableFuture.supplyAsync(() -> {
      System.out.println("Name set successfully for session: " + sessionId);
      sendStringMessage(session, "name", "");
      return redisService.getLastResult(); // Optional: return last Redis result
    }));
  }

  private void addRequestedRestaurant(WebSocketSession session, String restaurant) {
    String sessionId = session.getId();

    redisService.addCommandToQueue(redisService.sendKFVCommand("RPUSH", sessionId, "restaurants", restaurant));
  }

  private CompletableFuture<Boolean> addDoneMember(String groupName) {
    CompletableFuture<Boolean> resultFuture = new CompletableFuture<>();

    // Step 1: INCR done counter
    redisService.addCommandToQueue(redisService.sendKCommand("INCR", groupName + ":DoneMembers"));

    // Step 2: SCARD total members
    redisService.addCommandToQueue(redisService.sendKCommand("SCARD", groupName));

    // Step 3: Compute done >= total after both commands have executed
    redisService.addCommandToQueue(() -> CompletableFuture.supplyAsync(() -> {
      try {
        int done = Integer.parseInt(redisService.getLastResult().trim());

        // Note: We need total members value, which should have been stored in
        // lastResult
        // If you want to store multiple last results, you can extend RedisService to
        // keep a history
        // For simplicity, let's assume we get total from lastResult as well
        int total = Integer.parseInt(redisService.getLastResult().trim());

        boolean doneStatus = done >= total;
        resultFuture.complete(doneStatus);
      } catch (Exception e) {
        resultFuture.completeExceptionally(e);
      }
      return redisService.getLastResult(); // still need to return a String to satisfy type
    }));

    return resultFuture;
  }

  private void getUserActiveFriendsGroups(WebSocketSession session) {
    String sessionId = session.getId();
    List<String> friendSessionIds = Collections.synchronizedList(new ArrayList<>());
    List<String> activeGroups = Collections.synchronizedList(new ArrayList<>());

    // Step 1: Queue HGET for user's name
    redisService.addCommandToQueue(() -> redisService.sendKVCommand("HGET", sessionId, "name").get()
        .thenApply(name -> {
          if (name == null || name.trim().isEmpty()) {
            sendStringMessage(session, "noName", "You have not provided a name");
          } else {
            String trimmedName = name.trim();
            List<String> friendNames = userService.getFriends(trimmedName)
                .stream()
                .map(Friend::getName)
                .collect(Collectors.toList());

            // Step 2: Queue GET for each friend's sessionId
            for (String friendName : friendNames) {
              redisService.addCommandToQueue(() -> redisService.sendKCommand("GET", friendName).get()
                  .thenApply(sessionIdStr -> {
                    if (sessionIdStr != null && !sessionIdStr.trim().isEmpty()) {
                      friendSessionIds.add(sessionIdStr.trim());
                    }
                    return ""; // placeholder
                  }));
            }
          }
          return ""; // placeholder
        }));

    // Step 3: Queue HGET for each friend's group
    redisService.addCommandToQueue(() -> CompletableFuture.supplyAsync(() -> {
      for (String friendSessionId : friendSessionIds) {
        redisService.addCommandToQueue(() -> redisService.sendKVCommand("HGET", friendSessionId, "group").get()
            .thenApply(groupName -> {
              if (groupName != null && !groupName.trim().isEmpty()) {
                activeGroups.add(groupName.trim());
              }
              return ""; // placeholder
            }));
      }
      return ""; // placeholder
    }));

    // Step 4: Queue sending the final list to the session
    redisService.addCommandToQueue(() -> CompletableFuture.supplyAsync(() -> {
      List<String> distinctGroups = activeGroups.stream().distinct().collect(Collectors.toList());
      sendListMessage(session, "activeFriendsGroups", distinctGroups);
      return ""; // placeholder
    }));
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
    String sessionId = session.getId();

    // Step 1: remove name -> session mapping
    redisService.addCommandToQueue(redisService.sendKVCommand("HGET", sessionId, "name"));
    redisService.addCommandToQueue(() -> CompletableFuture.supplyAsync(() -> {
      String name = redisService.getLastResult();
      if (name != null && !name.trim().isEmpty()) {
        redisService.addCommandToQueue(redisService.sendKCommand("REM", name.trim()));
      }
      return ""; // placeholder
    }));

    // Step 2: remove session from group
    redisService.addCommandToQueue(redisService.sendKVCommand("HGET", sessionId, "group"));
    redisService.addCommandToQueue(() -> CompletableFuture.supplyAsync(() -> {
      String groupName = redisService.getLastResult();
      if (groupName != null && !groupName.trim().isEmpty()) {
        String g = groupName.trim();
        // remove this member
        redisService.addCommandToQueue(redisService.sendKVCommand("SREM", g, sessionId));
        // check if group is empty
        redisService.addCommandToQueue(redisService.sendKCommand("SCARD", g));
        redisService.addCommandToQueue(() -> CompletableFuture.supplyAsync(() -> {
          String membersLeft = redisService.getLastResult();
          if (membersLeft != null && membersLeft.trim().equals("0")) {
            redisService.addCommandToQueue(redisService.sendKCommand("SREM", g));
          }
          return "";
        }));
      }
      return "";
    }));

    // Step 3: delete session data
    redisService.addCommandToQueue(redisService.sendKCommand("DEL", sessionId));

    // Step 4: remove from session map
    sessionMap.remove(sessionId);
  }

  public void getRequestedGenresForGroup(String groupName, WebSocketSession session) {
    List<String> genres = Collections.synchronizedList(new ArrayList<>());

    // Step 1: Queue SGET for group members
    redisService.addCommandToQueue(() -> redisService.sendKCommand("SGET", groupName).get()
        .thenApply(membersCsv -> {
          if (membersCsv != null && !membersCsv.trim().isEmpty()) {
            String[] sessionNames = membersCsv.split(",");
            for (String name : sessionNames) {
              if (!name.trim().isEmpty()) {
                // Queue LRANGE for each member's genres
                redisService.addCommandToQueue(
                    () -> redisService.sendKFSECommand("LRANGE", name.trim(), "genres", "0", "-1").get()
                        .thenApply(result -> {
                          if (result != null && !result.trim().isEmpty()) {
                            genres.addAll(Arrays.asList(result.split(",")));
                          }
                          return ""; // placeholder
                        }));
              }
            }
          }
          return ""; // placeholder
        }));

    // Step 2: Queue sending the final genre list
    redisService.addCommandToQueue(() -> CompletableFuture.supplyAsync(() -> {
      sendListMessage(session, "requestedGenres", genres);
      return ""; // placeholder
    }));
  }

  public void getRequestedRestaurantsForGroup(String groupName, WebSocketSession session) {
    List<String> restaurants = Collections.synchronizedList(new ArrayList<>());

    // Step 1: Queue SGET for group members
    redisService.addCommandToQueue(() -> redisService.sendKCommand("SGET", groupName).get()
        .thenApply(membersCsv -> {
          if (membersCsv != null && !membersCsv.trim().isEmpty()) {
            String[] sessionNames = membersCsv.split(",");
            for (String name : sessionNames) {
              if (!name.trim().isEmpty()) {
                // Queue LRANGE for each member's restaurants
                redisService.addCommandToQueue(
                    () -> redisService.sendKFSECommand("LRANGE", name.trim(), "restaurants", "0", "-1").get()
                        .thenApply(result -> {
                          if (result != null && !result.trim().isEmpty()) {
                            restaurants.addAll(Arrays.asList(result.split(",")));
                          }
                          return ""; // placeholder
                        }));
              }
            }
          }
          return ""; // placeholder
        }));

    // Step 2: Queue sending the final restaurant list
    redisService.addCommandToQueue(() -> CompletableFuture.supplyAsync(() -> {
      sendListMessage(session, "requestedRestaurants", restaurants);
      return ""; // placeholder
    }));
  }

  public CompletableFuture<List<String>> getMatches(String groupName, List<String> requests) {
    CompletableFuture<List<String>> resultFuture = new CompletableFuture<>();

    // Step 1: queue SCARD to get group size
    redisService.addCommandToQueue(redisService.sendKCommand("SCARD", groupName));

    // Step 2: queue a task to process the result and compute matches
    redisService.addCommandToQueue(() -> CompletableFuture.supplyAsync(() -> {
      String groupSizeStr = redisService.getLastResult();
      int groupLen = 0;
      try {
        if (groupSizeStr != null && !groupSizeStr.trim().isEmpty()) {
          groupLen = Integer.parseInt(groupSizeStr.trim());
        }
      } catch (NumberFormatException e) {
        e.printStackTrace();
      }

      // compute matches
      List<String> matches = restaurantService.getMatches(requests, groupLen);

      // complete the future with the result
      resultFuture.complete(matches);
      return ""; // placeholder for the queued task
    }));

    return resultFuture;
  }

  public void getGroupMembers(String groupName, WebSocketSession session) {
    // Step 1: queue the SGET command
    redisService.addCommandToQueue(redisService.sendKCommand("SGET", groupName));

    // Step 2: queue a follow-up task to process the result
    redisService.addCommandToQueue(() -> CompletableFuture.supplyAsync(() -> {
      String response = redisService.getLastResult();
      String[] members;
      if (response == null || response.trim().isEmpty()) {
        members = new String[0];
      } else {
        members = Arrays.stream(response.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toArray(String[]::new);
      }

      // Optionally, send the members list to the WebSocket client
      sendListMessage(session, "groupMembers", Arrays.asList(members));

      return ""; // placeholder for the queued command
    }));
  }
}
