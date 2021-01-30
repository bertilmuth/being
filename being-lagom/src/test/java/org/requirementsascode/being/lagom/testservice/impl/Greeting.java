package org.requirementsascode.being.lagom.testservice.impl;

import static java.util.Objects.requireNonNull;

import java.time.LocalDateTime;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
class Greeting{
  public final String id;
  public final String text;
  public final String timestamp;

  private Greeting(String id, String text, String timestamp) {
    this.id = id;
    this.text = requireNonNull(text, "text must be non-null");
    this.timestamp = requireNonNull(timestamp, "timestamp must be non-null");
  }

  public static Greeting create(String id, String text) {
    return new Greeting(id, text, LocalDateTime.now().toString());
  }
}
