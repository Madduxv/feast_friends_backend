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
//import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.annotation.PostConstruct;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

  // Yes I know redis would wouk better. I will implement it later.
  private final Map<WebSocketSession, String> sessionNameMap = new ConcurrentHashMap<>();
  private final Map<String, WebSocketSession> nameSessionMap = new ConcurrentHashMap<>();
  private final Map<WebSocketSession, String> sessionGroupMap = new ConcurrentHashMap<>();
  private final Map<String, List<WebSocketSession>> groupSessionsMap = new ConcurrentHashMap<>();
  // private final Map<WebSocketSession, List<String>> requestedGenres = new
  // ConcurrentHashMap<>();
  // private final Map<WebSocketSession, List<String>> requestedRestaurants = new
  // ConcurrentHashMap<>();
  private final Map<String, Integer> groupDoneMap = new ConcurrentHashMap<>();
  // private final Map<String, Integer> groupActiveMap = new
  // ConcurrentHashMap<>();

  // private final RestaurantService restaurantService;
  // private final UserService userService;
  // private final RedisService redisService;

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
      // You might want to rethrow the exception or handle it based on your needs
    }
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    // Initialize session data if needed
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
      case "test":
        redisService.testRedis();
        break;
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
        if (addDoneMember(content)) {
          groupDoneMap.put(content, 0);
          broadcastMessageToGroup(session, "groupDoneStatus", "Everyone is done");
        }
        break;

      case "addGenre": // genre page
        addRequestedGenre(session, content); // content = genre
        break;

      case "getRequestedGenres": // genre page debug
        getRequestedGenresForGroup(content)
            .thenAccept(genres -> {
              if (genres != null && !genres.isEmpty()) {
                sendListMessage(session, "genres", genres); // content = groupName
              } else {
                System.out.println("No genres found for the group.");
              }
            })
            .exceptionally(ex -> {
              ex.printStackTrace();
              return null;
            });
        break;

      case "getGenreMatches": // user complete waiting page
        getRequestedGenresForGroup(content)
            .thenAccept(genres -> {
              if (genres != null && !genres.isEmpty()) {
                sendListMessage(session, "genreMatches", getMatches(content, genres));
              } else {
                System.out.println("No restaurants found for the group.");
              }
            })
            .exceptionally(ex -> {
              ex.printStackTrace();
              return null;
            });
        break;

      case "getRestaurantChoices": // user complete waiting page
        getRequestedGenresForGroup(content)
            .thenAccept(genres -> {
              if (genres != null && !genres.isEmpty()) {
                List<String> genreMatches = getMatches(content, genres);
                sendListMessage(session, "restaurants",
                    restaurantService.getRestaurantsWithRequestedGenre(genreMatches));
              } else {
                System.out.println("No genres found for the group.");
              }
            })
            .exceptionally(ex -> {
              ex.printStackTrace();
              return null;
            });
        break;

      case "addRestaurant": // restaurant page
        addRequestedRestaurant(session, content); // content = restaurant name
        break;

      case "getRequestedRestaurants": // restaurant page debug
        getRequestedRestaurantsForGroup(content)
            .thenAccept(restaurants -> {
              if (restaurants != null && !restaurants.isEmpty()) {
                sendListMessage(session, "groupRestaurants", restaurants);
              } else {
                System.out.println("No restaurants found for the group.");
              }
            })
            .exceptionally(ex -> {
              ex.printStackTrace();
              return null;
            });
        break;

      case "getRestaurantMatches": // results page
        getRequestedRestaurantsForGroup(content)
            .thenAccept(restaurants -> {
              if (restaurants != null && !restaurants.isEmpty()) {
                sendListMessage(session, "restaurantMatches", getMatches(content, restaurants));
              } else {
                System.out.println("No restaurants found for the group.");
              }
            })
            .exceptionally(ex -> {
              ex.printStackTrace();
              return null;
            });
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
    // Implement JSON parsing logic here
    // Return a map of key-value pairs from the JSON payload
    return new HashMap<>();
  }

  public void joinGroup(WebSocketSession session, String groupName) {

    String sessionId = session.getId();
    redisService.sendKVCommand("SADD", groupName, sessionId).thenAccept(response -> {
      if (response != null) {
        System.out.println(response);
      } else {
        System.out.println("No response received");
      }
    }).exceptionally(ex -> {
      // Handle any exceptions that occur during the async operation
      ex.printStackTrace();
      return null;
    });
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
  }

  private void broadcastMessageToGroup(WebSocketSession senderSession, String contentType, String message) {
    String groupName = sessionGroupMap.get(senderSession);
    List<WebSocketSession> groupSessions = groupSessionsMap.get(groupName);
    redisService.sendKCommand("SGET", groupName).thenAccept(response -> {
      if (response != null) {
        System.out.println(response);
      } else {
        System.out.println("No response received");
      }
    }).exceptionally(ex -> {
      // Handle any exceptions that occur during the async operation
      ex.printStackTrace();
      return null;
    });
    if (groupSessions != null) {
      for (WebSocketSession session : groupSessions) {
        if (session.isOpen()) {
          sendStringMessage(session, contentType, message);
        }
      }
    }
  }

  private void addRequestedGenre(WebSocketSession session, String genre) {
    redisService.addCommandToQueue(() -> {
      String sessionId = session.getId();
      redisService.sendKFVCommand("RPUSH", sessionId, "genres", genre).thenAccept(response -> {
        if (response != null) {
          System.out.println(response);
        } else {
          System.out.println("No response received");
        }
      }).exceptionally(ex -> {
        // Handle any exceptions that occur during the async operation
        ex.printStackTrace();
        return null;
      });
    });
  }

  private void addSessionName(WebSocketSession session, String name) {
    redisService.addCommandToQueue(() -> {
      String sessionId = session.getId();
      redisService.sendKFVCommand("HSET", sessionId, "name", name).thenAccept(response -> {
        if (response != null) {
          System.out.println(response);
        } else {
          System.out.println("No response received");
        }
      }).exceptionally(ex -> {
        // Handle any exceptions that occur during the async operation
        ex.printStackTrace();
        return null;
      });
    });
  }

  private void addRequestedRestaurant(WebSocketSession session, String restaurant) {
    redisService.addCommandToQueue(() -> {
      String sessionId = session.getId();
      redisService.sendKFVCommand("RPUSH", sessionId, "restaurants", restaurant).thenAccept(response -> {
        if (response != null) {
          System.out.println(response);
        } else {
          System.out.println("No response received");
        }
      }).exceptionally(ex -> {
        ex.printStackTrace();
        return null;
      });
    });
  }

  private boolean addDoneMember(String groupName) {
    redisService.addCommandToQueue(() -> {
      redisService.sendKCommand("INCR", groupName).thenAccept(response -> {
        if (response != null) {
          System.out.println(response);
        } else {
          System.out.println("No response received");
        }
      }).exceptionally(ex -> {
        // Handle any exceptions that occur during the async operation
        ex.printStackTrace();
        return null;
      });
    });
    groupDoneMap.compute(groupName, (key, value) -> (value == null) ? 0 : value + 1);
    if (getGroupSize(groupName) == groupDoneMap.get(groupName)) {
      return true;
    }
    return false;
  }

  private void getUserActiveFriendsGroups(WebSocketSession session) {
    List<String> userActiveFriendsGroups = new ArrayList<>();
    String friendsGroup;
    String name = sessionNameMap.getOrDefault(session, null);
    if (name == null) {
      sendStringMessage(session, "noName", "You have not provided a name");
      return;
    }
    List<Friend> usersFriends = userService.getFriends(name);
    List<String> usersFriendsNames = new ArrayList<>();
    for (Friend friend : usersFriends) {
      usersFriendsNames.add(friend.getName());
    }
    for (String friendName : usersFriendsNames) {
      if (nameSessionMap.containsKey(friendName)) {
        friendsGroup = sessionGroupMap.getOrDefault(nameSessionMap.get(friendName), "");
        if (!userActiveFriendsGroups.contains(friendsGroup)) {
          userActiveFriendsGroups.add(friendsGroup);
        }
      }
    }
    sendListMessage(session, "activeFriendsGroups", userActiveFriendsGroups);
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
  }

  // public CompletableFuture<List<String>> getRequestedGenresForGroup(String
  // groupName) {
  // List<String> genres = new ArrayList<>();
  //
  // return redisService.sendKCommand("SGET", groupName)
  // .thenCompose(sessionsData -> {
  // if (sessionsData == null || sessionsData.isEmpty()) {
  // return CompletableFuture.completedFuture(genres);
  // }
  //
  // String[] sessions = sessionsData.split(",");
  // List<CompletableFuture<Void>> futures = new ArrayList<>();
  //
  // for (String session : sessions) {
  // CompletableFuture<Void> future = new CompletableFuture<>();
  //
  // redisService.addCommandToQueue(() -> {
  // redisService.sendKFSECommand("LRANGE", session, "genres", "0", "-1")
  // .thenAccept(genresData -> {
  // if (genresData != null && !genresData.isEmpty()) {
  // synchronized (genres) {
  // System.out.println("Genres data for session " + session + ": " + genresData);
  // genres.addAll(Arrays.asList(genresData.split(",")));
  // }
  // }
  // future.complete(null);
  // })
  // .exceptionally(ex -> {
  // ex.printStackTrace();
  // future.completeExceptionally(ex);
  // return null;
  // });
  // });
  //
  // futures.add(future);
  // }
  //
  // return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
  // .thenApply(v -> genres);
  // })
  // .exceptionally(ex -> {
  // ex.printStackTrace();
  // return genres; // Return the genres collected so far or an empty list if
  // something goes wrong
  // });
  // }

  public CompletableFuture<List<String>> getRequestedGenresForGroup(String groupName) {
    List<String> genres = new ArrayList<>();

    return CompletableFuture.supplyAsync(() -> {
      // Step 1: Queue and execute the SGET command
      CompletableFuture<String> sgetFuture = new CompletableFuture<>();

      redisService.addCommandToQueue(() -> redisService.sendKCommand("SGET", groupName)
          .thenAccept(response -> {
            sgetFuture.complete(response);
          })
          .exceptionally(ex -> {
            sgetFuture.completeExceptionally(ex);
            return null;
          }));

      return sgetFuture;
    }).thenCompose(sessionsDataFuture -> {
      // Step 2: Handle the response from SGET
      return sessionsDataFuture.thenCompose(sessionsData -> {
        if (sessionsData == null || sessionsData.isEmpty()) {
          return CompletableFuture.completedFuture(genres);
        }

        String[] sessions = sessionsData.split(",");
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (String session : sessions) {
          CompletableFuture<Void> future = new CompletableFuture<>();

          // Step 3: Queue and execute LRANGE commands for each session
          redisService.addCommandToQueue(() -> redisService.sendKFSECommand("LRANGE", session, "genres", "0", "-1")
              .thenAccept(genresData -> {
                if (genresData != null && !genresData.isEmpty()) {
                  synchronized (genres) {
                    genres.addAll(Arrays.asList(genresData.split(",")));
                  }
                }
                future.complete(null);
              })
              .exceptionally(ex -> {
                future.completeExceptionally(ex);
                return null;
              }));
          futures.add(future);
        }

        // Step 4: Ensure all LRANGE commands have completed before returning the genres
        // list
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> genres);
      });
    }).exceptionally(ex -> {
      ex.printStackTrace();
      return genres;
    });
  }

  public CompletableFuture<List<String>> getRequestedRestaurantsForGroup(String groupName) {
    List<String> restaurants = new ArrayList<>();

    return CompletableFuture.supplyAsync(() -> {
      CompletableFuture<String> sgetFuture = new CompletableFuture<>();

      // Queue the SGET command
      redisService.addCommandToQueue(() -> redisService.sendKCommand("SGET", groupName)
          .thenAccept(response -> {
            sgetFuture.complete(response);
          })
          .exceptionally(ex -> {
            sgetFuture.completeExceptionally(ex);
            return null;
          }));

      return sgetFuture;
    }).thenCompose(sessionsDataFuture -> {
      // Handle the response from the SGET command
      return sessionsDataFuture.thenCompose(sessionsData -> {
        if (sessionsData == null || sessionsData.isEmpty()) {
          return CompletableFuture.completedFuture(restaurants);
        }

        String[] sessions = sessionsData.split(",");
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (String session : sessions) {
          CompletableFuture<Void> future = new CompletableFuture<>();
          // Queue the LRANGE commands for each session
          redisService.addCommandToQueue(() -> redisService.sendKFSECommand("LRANGE", session, "restaurants", "0", "-1")
              .thenAccept(restaurantsData -> {
                if (restaurantsData != null && !restaurantsData.isEmpty()) {
                  synchronized (restaurants) {
                    restaurants.addAll(Arrays.asList(restaurantsData.split(",")));
                  }
                }
                future.complete(null);
              })
              .exceptionally(ex -> {
                future.completeExceptionally(ex);
                return null;
              }));
          futures.add(future);
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> restaurants);
      });
    }).exceptionally(ex -> {
      ex.printStackTrace();
      System.out.println(restaurants);
      return restaurants;
    });
  }

  // public CompletableFuture<List<String>> getRequestedRestaurantsForGroup(String
  // groupName) {
  // List<String> restaurants = new ArrayList<>();
  //
  // return redisService.sendKCommand("SGET", groupName)
  // .thenCompose(sessionsData -> {
  // if (sessionsData == null || sessionsData.isEmpty()) {
  // return CompletableFuture.completedFuture(restaurants);
  // }
  //
  // String[] sessions = sessionsData.split(",");
  // List<CompletableFuture<Void>> futures = new ArrayList<>();
  //
  // for (String session : sessions) {
  // CompletableFuture<Void> future = new CompletableFuture<>();
  //
  // redisService.addCommandToQueue(() -> {
  // redisService.sendKFSECommand("LRANGE", session, "restaurants", "0", "-1")
  // .thenAccept(restaurantsData -> {
  // if (restaurantsData != null && !restaurantsData.isEmpty()) {
  // synchronized (restaurants) {
  // System.out.println("Restaurants data for session " + session + ": " +
  // restaurantsData);
  // restaurants.addAll(Arrays.asList(restaurantsData.split(",")));
  // }
  // }
  // future.complete(null);
  // })
  // .exceptionally(ex -> {
  // ex.printStackTrace();
  // future.completeExceptionally(ex);
  // return null;
  // });
  // });
  //
  // futures.add(future);
  // }
  //
  // return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
  // .thenApply(v -> restaurants);
  // })
  // .exceptionally(ex -> {
  // ex.printStackTrace();
  // return restaurants; // Return the restaurants collected so far or an empty
  // list if something goes
  // // wrong
  // });
  // }

  public List<String> getMatches(String groupName, List<String> requests) {
    int groupLength = groupSessionsMap.get(groupName).size();
    return restaurantService.getMatches(requests, groupLength);
  }

  public CompletableFuture<String[]> getGroupMembers(String groupName) {
    CompletableFuture<String[]> futureGroupMembers = new CompletableFuture<>();

    redisService.sendKCommand("SGET", groupName).thenAccept(response -> {
      if (response != null) {
        String[] groupMembers = response.split(",");
        futureGroupMembers.complete(groupMembers);
      } else {
        System.out.println("No response received");
        futureGroupMembers.complete(new String[0]); // Return an empty array if no response
      }
    }).exceptionally(ex -> {
      ex.printStackTrace();
      futureGroupMembers.completeExceptionally(ex);
      return null;
    });

    return futureGroupMembers;
  }
}
