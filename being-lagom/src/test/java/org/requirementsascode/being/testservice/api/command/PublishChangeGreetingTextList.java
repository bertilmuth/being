package org.requirementsascode.being.testservice.api.command;

import org.requirementsascode.being.Properties;

import lombok.Value;

@Value @Properties
public class PublishChangeGreetingTextList{
  String firstGreeting;
  String secondGreeting;
}
