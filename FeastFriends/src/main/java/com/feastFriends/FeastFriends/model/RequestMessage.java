package com.feastFriends.feastFriends.model;

public class RequestMessage {
  private String action;
  private String content;

  public RequestMessage() {}

  public RequestMessage(String action, String content) {
    this.action = action;
    this.content = content;
  }

  public String getAction() {
    return action;
  }

  public String getContent() {
    return content;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public void setContent(String content) {
    this.content = content;
  }
}
