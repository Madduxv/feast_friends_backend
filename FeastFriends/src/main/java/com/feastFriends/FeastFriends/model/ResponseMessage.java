package com.feastFriends.feastFriends.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ResponseMessage {

  @JsonProperty("contentType")
  private String contentType;

  @JsonProperty("content")
  private List<String> content;

  public ResponseMessage() {}

  public ResponseMessage(String contentType, List<String> content) {
    this.contentType = contentType;
    this.content = content;
  }

  public String getContentType() {
    return contentType;
  }

  public List<String> getContent() {
    return content;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public void setContent(List<String> content) {
    this.content = content;
  }
}
