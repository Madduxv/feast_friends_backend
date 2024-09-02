package com.feastFriends.feastFriends.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;

public class RedisService {
  private Socket socket;
  private PrintWriter out;
  private BufferedReader in;

  public RedisService(String host, int port) throws Exception {
    this.socket = new Socket(host, port);
    this.out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
    this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    warmUpConnection();
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
  }

  private String getResponse() {
    try {
      return in.readLine();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public CompletableFuture<String> sendKFSECommand(String command, String key, String field, int start, int end) {
    return CompletableFuture.supplyAsync(() -> {
      out.printf("\n%s\n%s\n%s\n%s\n", command, key, field, start, end);
      out.flush();
      return getResponse();
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
}
