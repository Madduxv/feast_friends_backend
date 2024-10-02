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

  private final Map<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

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
      // You might want to rethrow the exception or handle it based on your needs
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

      case "getRequestedGenres": // genre page debug
        getRequestedGenresForGroup(content) // content = groupName
            .thenAccept(genres -> {
              if (genres != null && !genres.isEmpty()) {
                sendListMessage(session, "genres", genres);
              } else {
                sendListMessage(session, "genres", new ArrayList<>());
              }
            })
            .exceptionally(ex -> {
              ex.printStackTrace();
              return null;
            });
        break;

      case "getGenreMatches": // user complete waiting page
        getRequestedGenresForGroup(content) // content = groupName
            .thenAccept(genres -> {
              if (genres != null && !genres.isEmpty()) {
                sendListMessage(session, "genreMatches", getMatches(content, genres));
              } else {
                sendListMessage(session, "genreMatches", new ArrayList<>());
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
                sendListMessage(session, "restaurants", new ArrayList<>());
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
        getRequestedRestaurantsForGroup(content) // content = groupName
            .thenAccept(restaurants -> {
              if (restaurants != null && !restaurants.isEmpty()) {
                sendListMessage(session, "groupRestaurants", restaurants);
              } else {
                sendListMessage(session, "groupRestaurants", new ArrayList<>());
              }
            })
            .exceptionally(ex -> {
              ex.printStackTrace();
              return null;
            });
        break;

      case "getRestaurantMatches": // results page
        getRequestedRestaurantsForGroup(content) // content = groupName
            .thenAccept(restaurants -> {
              if (restaurants != null && !restaurants.isEmpty()) {
                sendListMessage(session, "restaurantMatches", getMatches(content, restaurants));
              } else {
                sendListMessage(session, "restaurantMatches", new ArrayList<>());
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
    return new HashMap<>();
  }

  public void joinGroup(WebSocketSession session, String groupName) {
    String sessionId = session.getId();

    redisService.addCommandToQueue(() -> redisService.sendKVCommand("HGET", sessionId, "name").thenCompose(name -> {
      if (name != null) {
        return redisService.sendKVCommand("SADD", groupName, name)
            .thenCompose(addResponse -> redisService.sendKVCommand("HGET", sessionId, "group")
                .thenCompose(oldGroup -> {
                  if (oldGroup != null && !oldGroup.equals(groupName) && !oldGroup.equals("(nil)")) {
                    return redisService.sendKVCommand("SREM", oldGroup, name)
                        .thenCompose(
                            removeResponse -> redisService.sendKFVCommand("HSET", sessionId, "group", groupName));
                  } else {
                    return redisService.sendKFVCommand("HSET", sessionId, "group", groupName);
                  }
                }))
            .thenAccept(finalResponse -> System.out.printf("Session %s joined %s\n", sessionId, groupName));
      } else {
        System.out.println("Error getting session's name");
        return CompletableFuture.completedFuture(null);
      }
    }).exceptionally(ex -> {
      ex.printStackTrace();
      return null;
    }));
  }

  private void broadcastMessageToGroup(WebSocketSession senderSession, String contentType, String message) {
    redisService.addCommandToQueue(() -> {
      redisService.sendKVCommand("HGET", senderSession.getId(), "group").thenAccept(groupName -> {

        redisService.addCommandToQueue(() -> {
          redisService.sendKCommand("SGET", groupName).thenAccept(usernames -> {
            if (usernames == null && usernames != "") {
              System.out.println("No response received for SGET command");
            } else {

              redisService.addCommandToQueue(() -> {
                for (String username : usernames.split("[,]", 0)) {
                  redisService.addCommandToQueue(() -> {
                    redisService.sendKCommand("GET", username).thenAccept(sessionId -> {
                      if (sessionMap.get(sessionId).isOpen()) {
                        sendStringMessage(sessionMap.get(sessionId), contentType, message);
                      }

                    }).exceptionally(ex -> {
                      ex.printStackTrace();
                      return null;
                    });
                  });
                }
              });
            }

          }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
          });
        });

      }).exceptionally(ex -> {
        ex.printStackTrace();
        return null;
      });
    });

  }

  private void addRequestedGenre(WebSocketSession session, String genre) {
    redisService.addCommandToQueue(() -> {
      String sessionId = session.getId();
      redisService.sendKFVCommand("RPUSH", sessionId, "genres", genre).thenAccept(response -> {
        if (response == null) {
          System.out.println("No response received");
        }
      }).exceptionally(ex -> {
        ex.printStackTrace();
        return null;
      });
    });
  }

  private void addSessionName(WebSocketSession session, String name) {
    String sessionId = session.getId();

    redisService.addCommandToQueue(() -> {

      redisService.sendKFVCommand("HSET", sessionId, "name", name)
          .thenAccept(response -> {
            if (response == null || response.contains("ERR")) {
              System.out.println("Error setting name: " + response);

            } else {
              System.out.println("Name set successfully for session: " + sessionId);
              sendStringMessage(session, "name", "");
            }
          }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
          });
    });
  }

  // redisService.addCommandToQueue(() -> {
  // String sessionId = session.getId();
  // redisService.sendKVCommand("SET", name, sessionId).thenAccept(response -> {
  // if (response == null) {
  // System.out.println("No response received");
  // }
  // }).exceptionally(ex -> {
  // ex.printStackTrace();
  // return null;
  // });
  // });
  // }

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

  private CompletableFuture<Boolean> addDoneMember(String groupName) {
    CompletableFuture<Boolean> groupDoneFuture = new CompletableFuture<>();
    redisService.addCommandToQueue(() -> {
      redisService.sendKCommand("INCR", groupName + ":DoneMembers").thenAccept(doneMembers -> {
        if (doneMembers == null) {
          System.out.println("No response received for INCR command");
          return;
        } else {

          redisService.addCommandToQueue(() -> {
            redisService.sendKCommand("SCARD", groupName).thenAccept(groupMembers -> {
              if (groupMembers == null) {
                System.out.println("No response received for SCARD command");
                return;
              }
              if (Integer.parseInt(doneMembers) != Integer.parseInt(groupMembers)) {
                groupDoneFuture.complete(false);
              } else {
                groupDoneFuture.complete(true);
              }

            }).exceptionally(ex -> {
              ex.printStackTrace();
              return null;
            });
          });
        }
      }).exceptionally(ex -> {
        ex.printStackTrace();
        return null;
      });
    });
    return groupDoneFuture;
  }

  private void getUserActiveFriendsGroups(WebSocketSession session) {
    CompletableFuture<List<String>> userActiveFriendsGroupsFuture = new CompletableFuture<>();
    CompletableFuture<List<String>> userActiveFriendsFuture = new CompletableFuture<>();
    List<String> usersFriendsNames = new ArrayList<>();

    redisService.addCommandToQueue(() -> {
      redisService.sendKVCommand("HGET", session.getId(), "name").thenAccept(name -> {
        if (name == null || name.trim() == "") {
          sendStringMessage(session, "noName", "You have not provided a name");
          userActiveFriendsGroupsFuture.complete(new ArrayList<>());
          return;
        }

        List<Friend> usersFriends = userService.getFriends(name.trim());
        for (Friend friend : usersFriends) {
          usersFriendsNames.add(friend.getName());
        }

        redisService.addCommandToQueue(() -> {
          List<String> activeFriends = new ArrayList<>();
          for (String friendName : usersFriendsNames) {
            redisService.sendKCommand("GET", friendName).thenAccept(friendSession -> {
              if (friendSession != null && friendSession.trim() != "") {
                activeFriends.add(friendSession);
              }
            });
          }
          userActiveFriendsFuture.complete(activeFriends);
        });

        redisService.addCommandToQueue(() -> {
          try {
            List<String> activeFriends = userActiveFriendsFuture.get();
            List<String> activeGroups = new ArrayList<>();
            for (String friend : activeFriends) {
              redisService.addCommandToQueue(() -> {
                redisService.sendKVCommand("HGET", friend, "group").thenAccept(group -> {
                  if (!activeGroups.contains(group) && group != null && group.trim() != "") {
                    activeGroups.add(group);
                  }
                });
              });
            }
            userActiveFriendsGroupsFuture.complete(activeGroups);
          } catch (Exception e) {
            userActiveFriendsGroupsFuture.complete(new ArrayList<>());
            e.printStackTrace();
          }
        });
      });
    });
    try {
      sendListMessage(session, "activeFriendsGroups", userActiveFriendsGroupsFuture.get());
    } catch (Exception e) {
      sendListMessage(session, "activeFriendsGroups", new ArrayList<>());
      e.printStackTrace();
    }
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

    redisService.addCommandToQueue(() -> {
      redisService.sendKVCommand("HGET", session.getId(), "name").thenAccept(name -> {
        if (name != null && name != "") {
          redisService.addCommandToQueue(() -> {
            redisService.sendKCommand("REM", name);
          });
        }
      });
    });

    redisService.addCommandToQueue(() -> {
      redisService.sendKVCommand("HGET", session.getId(), "group").thenAccept(groupName -> {
        if (groupName != null && groupName != "") {
          redisService.addCommandToQueue(() -> {
            redisService.sendKVCommand("SREM", groupName, session.getId());
          });

          redisService.addCommandToQueue(() -> {
            redisService.sendKCommand("SCARD", groupName).thenAccept(membersLeft -> {
              if (membersLeft == "0") {
                redisService.addCommandToQueue(() -> {
                  redisService.sendKCommand("SREM", groupName);
                });
              }
            });
          });

        }
      });
    });

    redisService.addCommandToQueue(() -> {
      redisService.sendKCommand("DEL", session.getId());
    });

    sessionMap.remove(session.getId());
  }

  public CompletableFuture<List<String>> getRequestedGenresForGroup(String groupName) {
    List<String> genres = new ArrayList<>();

    return CompletableFuture.supplyAsync(() -> {
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
      return sessionsDataFuture.thenCompose(sessionsData -> {
        if (sessionsData == null || sessionsData.isEmpty()) {
          return CompletableFuture.completedFuture(genres);
        }

        String[] sessions = sessionsData.split(",");
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (String session : sessions) {
          CompletableFuture<Void> future = new CompletableFuture<>();

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
      return sessionsDataFuture.thenCompose(sessionsData -> {
        if (sessionsData == null || sessionsData.isEmpty()) {
          return CompletableFuture.completedFuture(restaurants);
        }

        String[] sessions = sessionsData.split(",");
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (String session : sessions) {
          CompletableFuture<Void> future = new CompletableFuture<>();
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

  public List<String> getMatches(String groupName, List<String> requests) {
    CompletableFuture<Integer> groupLengthFuture = new CompletableFuture<>();

    redisService.addCommandToQueue(() -> {
      redisService.sendKCommand("SCARD", groupName).thenAccept(groupSize -> {
        if (groupSize != null) {

          try {
            Integer groupLen = Integer.parseInt(groupSize);
            groupLengthFuture.complete(groupLen);
          } catch (Exception e) {
            e.printStackTrace();
          }

        }
      });
    });

    try {
      return restaurantService.getMatches(requests, groupLengthFuture.get());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new ArrayList<>();
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
