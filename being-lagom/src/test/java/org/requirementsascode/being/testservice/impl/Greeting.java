package org.requirementsascode.being.testservice.impl;

import static java.util.Objects.requireNonNull;

import java.time.LocalDateTime;

import lombok.Value;

@Value
class Greeting{
  String id;
  String text;
  String timestamp;

  private Greeting(String id, String text, String timestamp) {
    this.id = id;
    this.text = requireNonNull(text, "text must be non-null");
    this.timestamp = requireNonNull(timestamp, "timestamp must be non-null");
  }

  public static Greeting create(String id, String text) {
    return new Greeting(id, text, LocalDateTime.now().toString());
  }
}
