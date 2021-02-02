package org.requirementsascode.being.testservice.impl;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.requirementsascode.Model;
import org.requirementsascode.being.AggregateBehavior;
import org.requirementsascode.being.Properties;
import org.requirementsascode.being.testservice.api.command.ChangeGreetingText;
import org.requirementsascode.being.testservice.api.command.FailingUpdateAggregateRoot;
import org.requirementsascode.being.testservice.api.command.IgnoredCommand;
import org.requirementsascode.being.testservice.api.command.IgnoredUpdateAggregateRoot;
import org.requirementsascode.being.testservice.api.command.PublishChangeGreetingTextList;
import org.requirementsascode.being.testservice.api.command.PublishChangeGreetingTextSet;
import org.requirementsascode.being.testservice.api.response.GreetingResponse;

import lombok.Value;

class GreetUserBehavior extends AggregateBehavior<Greeting>{
  @Override
  public Greeting createAggregateRoot(String aggregateId) {
    return Greeting.create(aggregateId, "Hello");
  }
  
  @Override
  public Object responseMessage() {
    return new GreetingResponse(aggregateRoot().getText() + ", " + aggregateRoot().getId() + "!");
  }
  
  @Override
  public Model incomingMessageHandlers() {
    Model model = Model.builder()
      .user(IgnoredCommand.class).system(cmd -> {})
      .user(ChangeGreetingText.class).systemPublish(this::greetingTextChanged)
      .user(PublishChangeGreetingTextList.class).systemPublish(this::publishChangeGreetingTextList)
      .user(PublishChangeGreetingTextSet.class).systemPublish(this::publishChangeGreetingTextSet)
      .on(FailingUpdateAggregateRoot.class).systemPublish(ev -> new FailingUpdateAggregateRootEvent())
      .on(IgnoredUpdateAggregateRoot.class).systemPublish(ev -> new IgnoredUpdateAggregateRootEvent())
      .build();
    return model;
  }
  
  @Override
  public Model internalEventHandlers() {
    Model model = Model.builder()
      .on(GreetingTextChanged.class).systemPublish(this::newGreeting)
      .on(FailingUpdateAggregateRootEvent.class).systemPublish(ev -> "This should be an aggregate root, so it will fail!")
      .on(IgnoredUpdateAggregateRootEvent.class).system(ev -> {})
      .build();
    return model;
  }
  
  private GreetingTextChanged greetingTextChanged(ChangeGreetingText command) {
    String newText = command.getNewText();
    if(newText.isEmpty()) {
      throw new RuntimeException("Text must not be empty!");
    }
    
    GreetingTextChanged event = new GreetingTextChanged(newText);
    return event;
  }
  
  private Greeting newGreeting(GreetingTextChanged event) {
    return Greeting.create(aggregateRoot().getId(), event.getText()); 
  }
  
  private List<GreetingTextChanged> publishChangeGreetingTextList(PublishChangeGreetingTextList command) {
    GreetingTextChanged testEvent1 = greetingTextChanged(new ChangeGreetingText(command.getFirstGreeting()));
    GreetingTextChanged testEvent2 = greetingTextChanged(new ChangeGreetingText(command.getSecondGreeting()));
    List<GreetingTextChanged> greetingTextChangedList = Arrays.asList(testEvent1, testEvent2);
    return greetingTextChangedList;
  }
  
  private Set<GreetingTextChanged> publishChangeGreetingTextSet(PublishChangeGreetingTextSet command) {
    GreetingTextChanged testEvent1 = greetingTextChanged(new ChangeGreetingText(command.getFirstGreeting()));
    GreetingTextChanged testEvent2 = greetingTextChanged(new ChangeGreetingText(command.getSecondGreeting()));
    List<GreetingTextChanged> greetingTextChangedList = Arrays.asList(testEvent1, testEvent2);
    return new LinkedHashSet<>(greetingTextChangedList);
  }
  
  // Internal event classes
  
  @Value @Properties
  static final class GreetingTextChanged{
    String text;
  }
  
  @Properties
  static class FailingUpdateAggregateRootEvent {
  }
  
  @Properties
  static final class IgnoredUpdateAggregateRootEvent{
  }
}