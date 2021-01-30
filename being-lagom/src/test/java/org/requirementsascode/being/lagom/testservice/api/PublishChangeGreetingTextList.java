package org.requirementsascode.being.lagom.testservice.api;

import org.requirementsascode.being.Properties;

import lombok.Value;

@Value @Properties
public class PublishChangeGreetingTextList{
  String firstGreeting;
  String secondGreeting;
}
