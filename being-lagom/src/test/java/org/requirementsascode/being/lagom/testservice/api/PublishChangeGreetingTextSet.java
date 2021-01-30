package org.requirementsascode.being.lagom.testservice.api;

import org.requirementsascode.being.Properties;

import lombok.Value;

@Value @Properties
public class PublishChangeGreetingTextSet{
  String firstGreeting;
  String secondGreeting;
}
