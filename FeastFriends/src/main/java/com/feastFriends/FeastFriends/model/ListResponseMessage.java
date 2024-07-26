package com.feastFriends.feastFriends.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ListResponseMessage {

  @JsonProperty("contentType")
  private String contentType;

  @JsonProperty("content")
  private List<String> content;

  public ListResponseMessage() {}

  public ListResponseMessage(String contentType, List<String> content) {
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
