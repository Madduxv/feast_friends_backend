package com.feastFriends.feastFriends.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

public class RedisService {
  private Socket socket;
  private PrintWriter out;
  private BufferedReader in;

  public RedisService(String host, int port) throws Exception {
    this.socket = new Socket(host, port);
    this.out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
    this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    addCommandToQueue(() -> {
      try {
        warmUpConnection();
        System.out.println(in.readLine());
        out.flush();
      } catch (IOException e) {
        // dont care
      }
    });

  }

  Queue<Runnable> commandQueue = new LinkedList<>();

  void executeNextCommand() {
    if (!commandQueue.isEmpty()) {
      Runnable command = commandQueue.poll();
      new Thread(() -> {
        command.run();
        executeNextCommand(); // Execute the next command only after the current one completes
      }).start();
    }
  }

  public void addCommandToQueue(Runnable command) {
    commandQueue.add(() -> {
      command.run();
      executeNextCommand(); // Ensure the next command is executed after the current one
    });

    // Start execution if the queue was empty
    if (commandQueue.size() == 1) {
      executeNextCommand();
    }
  }

  public void close() throws Exception {
    in.close();
    out.close();
    socket.close();
  }

  private void warmUpConnection() throws IOException {
    out.write("PING\r\n");
    out.flush();
    in.readLine(); // Read the response to ensure the connection is ready
    out.flush();
  }

  private String getResponse() {
    try {
      String response = null;
      while (response == null) {
        response = in.readLine();
      }
      out.flush();
      return response;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public CompletableFuture<String> sendKFSECommand(String command, String key, String field, String start, String end) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        // Assuming you write the command to the Redis server
        out.printf("\n%s\n%s\n%s\n%s\n%s\n", command, key, field, start, end);
        out.flush();
        return getResponse();
      } catch (Exception e) {
        e.printStackTrace();
        return null;
      }
    });
  }

  // HGET, HSET, ...
  public CompletableFuture<String> sendKFVCommand(String command, String key, String field, String value) {
    return CompletableFuture.supplyAsync(() -> {
      out.printf("\n%s\n%s\n%s\n%s\n", command, key, field, value);
      out.flush();
      return getResponse();
    });
  }

  // SADD, SREM, ...
  public CompletableFuture<String> sendKVCommand(String command, String key, String value) {
    return CompletableFuture.supplyAsync(() -> {
      out.printf("\n%s\n%s\n%s\n", command, key, value);
      out.flush();
      return getResponse();
    });
  }

  // DEL, ...
  public CompletableFuture<String> sendKCommand(String command, String key) {
    return CompletableFuture.supplyAsync(() -> {
      out.printf("\n%s\n%s\n", command, key);
      out.flush();
      return getResponse();
    });
  }

  public CompletableFuture<String> sendPingCommand() {
    return CompletableFuture.supplyAsync(() -> {
      out.print("PING\r\n");
      out.flush();
      return getResponse();
    });

  }

  public void testRedis() {
    List<String> responses = new ArrayList<String>();
    String key = "testKey";
    addCommandToQueue(() -> sendKFVCommand("HSET", key, "name", "Maddux")
        .thenAccept(response -> {
          responses.add("HSET: " + response);
        })
        .exceptionally(ex -> {
          ex.printStackTrace();
          return null;
        }));

    addCommandToQueue(() -> sendKVCommand("SADD", "testSet", "testValue1")
        .thenAccept(response -> {
          responses.add("SADD: " + response);
        })
        .exceptionally(ex -> {
          ex.printStackTrace();
          return null;
        }));

    addCommandToQueue(() -> sendKVCommand("SADD", "testSet", "testValue2")
        .thenAccept(response -> {
          responses.add("SADD: " + response);
        })
        .exceptionally(ex -> {
          ex.printStackTrace();
          return null;
        }));

    addCommandToQueue(() -> sendKCommand("SGET", "testSet")
        .thenAccept(response -> {
          responses.add("SGET: " + response);
        })
        .exceptionally(ex -> {
          ex.printStackTrace();
          return null;
        }));

    addCommandToQueue(() -> sendKFVCommand("RPUSH", key, "testField", "testValue1")
        .thenAccept(response -> {
          responses.add("RPUSH: " + response);
        })
        .exceptionally(ex -> {
          ex.printStackTrace();
          return null;
        }));

    addCommandToQueue(() -> sendKFVCommand("RPUSH", key, "testField", "testValue2")
        .thenAccept(response -> {
          responses.add("RPUSH: " + response);
        })
        .exceptionally(ex -> {
          ex.printStackTrace();
          return null;
        }));

    addCommandToQueue(() -> sendKFVCommand("RPUSH", key, "testField", "testValue3")
        .thenAccept(response -> {
          responses.add("RPUSH: " + response);
        })
        .exceptionally(ex -> {
          ex.printStackTrace();
          return null;
        }));

    addCommandToQueue(() -> sendKFSECommand("LRANGE", key, "testField", "0", "-1")
        .thenAccept(response -> {
          responses.add("LRANGE: " + response);
        })
        .exceptionally(ex -> {
          ex.printStackTrace();
          return null;
        }));

    addCommandToQueue(() -> sendKCommand("DEL", key)
        .thenAccept(response -> {
          responses.add("DEL: " + response);
        })
        .exceptionally(ex -> {
          ex.printStackTrace();
          return null;
        }));

    addCommandToQueue(() -> sendKCommand("SDEL", "testSet")
        .thenAccept(response -> {
          responses.add("SDEL: " + response);
        })
        .exceptionally(ex -> {
          ex.printStackTrace();
          return null;
        }));
    System.out.println(responses);
  }
}
