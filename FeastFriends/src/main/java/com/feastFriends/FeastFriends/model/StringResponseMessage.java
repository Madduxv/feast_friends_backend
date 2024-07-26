package com.feastFriends.feastFriends.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StringResponseMessage {

  @JsonProperty("contentType")
  private String contentType;

  @JsonProperty("content")
  private String content;

  public StringResponseMessage() {}

  public StringResponseMessage(String contentType, String content) {
    this.contentType = contentType;
    this.content = content;
  }

  public String getContentType() {
    return contentType;
  }

  public String getContent() {
    return content;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public void setContent(String content) {
    this.content = content;
  }
}
