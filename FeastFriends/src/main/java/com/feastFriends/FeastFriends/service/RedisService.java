package com.feastFriends.feastFriends.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Queue;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class RedisService {
  private final Socket socket;
  private final PrintWriter out;
  private final BufferedReader in;

  private final Queue<Supplier<CompletableFuture<String>>> commandQueue = new LinkedList<>();
  private boolean isRunning = false;

  // Stores the result of the last executed command
  private String lastResult;

  public RedisService(String host, int port) throws IOException {
    this.socket = new Socket(host, port);
    this.out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
    this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
  }

  /**
   * Add a command to the queue for sequential execution.
   */
  public synchronized void addCommandToQueue(Supplier<CompletableFuture<String>> command) {
    commandQueue.add(() -> {
      CompletableFuture<String> future = command.get();
      future.thenAccept(result -> lastResult = result); // Save last result
      return future;
    });

    if (!isRunning) {
      runNextCommand();
    }
  }

  /**
   * Execute commands in the queue sequentially.
   */
  private synchronized void runNextCommand() {
    Supplier<CompletableFuture<String>> command = commandQueue.poll();
    if (command != null) {
      isRunning = true;
      command.get().whenComplete((res, ex) -> {
        if (ex != null) {
          ex.printStackTrace();
        }
        runNextCommand();
      });
    } else {
      isRunning = false;
    }
  }

  /**
   * Get the result of the last executed command.
   */
  public synchronized String getLastResult() {
    return lastResult;
  }

  /**
   * Send a raw command to Redis and return a CompletableFuture for its response.
   */
  public CompletableFuture<String> sendCommand(String... args) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        for (String arg : args) {
          out.println(arg);
        }
        out.flush();
        return readResponse();
      } catch (IOException e) {
        e.printStackTrace();
        return null;
      }
    });
  }

  /**
   * Read a single line response from Redis.
   */
  private String readResponse() throws IOException {
    String response = in.readLine();
    return response != null ? response.trim() : null;
  }

  // --- Convenience wrappers for common commands ---

  public Supplier<CompletableFuture<String>> sendKFVCommand(String command, String key, String field, String value) {
    return () -> sendCommand(command, key, field, value);
  }

  public Supplier<CompletableFuture<String>> sendKFSECommand(String command, String key, String field, String upper,
      String lower) {
    return () -> sendCommand(command, key, field, upper, lower);
  }

  public Supplier<CompletableFuture<String>> sendKVCommand(String command, String key, String value) {
    return () -> sendCommand(command, key, value);
  }

  public Supplier<CompletableFuture<String>> sendKCommand(String command, String key) {
    return () -> sendCommand(command, key);
  }

  public Supplier<CompletableFuture<String>> sendPingCommand() {
    return () -> sendCommand("PING");
  }

  /**
   * Close the Redis connection.
   */
  public void close() throws IOException {
    in.close();
    out.close();
    socket.close();
  }
}
