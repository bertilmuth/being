package org.requirementsascode.being.testservice.impl;

import java.time.LocalDateTime;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
class Greeting{
  private final String id;
  private final String text;
  private final String timestamp;
  
  private Greeting(String id, String text, String timestamp) {
    this.id = id;
    this.text = text;
    this.timestamp = timestamp;
  }

  public static Greeting create(String id, String text) {
    return new Greeting(id, text, LocalDateTime.now().toString());
  }
  
  public String getId() {
    return id;
  }

  public String getText() {
    return text;
  }

  public String getTimestamp() {
    return timestamp;
  }
}
