package org.requirementsascode.being.testservice.impl;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.requirementsascode.Model;
import org.requirementsascode.being.AggregateBehavior;
import org.requirementsascode.being.Properties;
import org.requirementsascode.being.testservice.api.ChangeGreetingText;
import org.requirementsascode.being.testservice.api.GreetingResponse;
import org.requirementsascode.being.testservice.api.IgnoredCommand;
import org.requirementsascode.being.testservice.api.PublishChangeGreetingTextList;
import org.requirementsascode.being.testservice.api.PublishChangeGreetingTextSet;
import org.requirementsascode.being.testservice.api.TestFailingUpdateAggregateRootCommand;
import org.requirementsascode.being.testservice.api.TestFailingUpdateAggregateRootEvent;

import lombok.Value;

class GreetUserBehavior extends AggregateBehavior<Greeting>{
  @Override
  public Greeting createAggregateRoot(String aggregateId) {
    return Greeting.create(aggregateId, "Hello");
  }
  
  @Override
  public Object responseMessage() {
    return new GreetingResponse(aggregateRoot().text + ", " + aggregateRoot().id + "!");
  }
  
  @Override
  public Model incomingMessageHandlers() {
    Model model = Model.builder()
      .user(IgnoredCommand.class).system(cmd -> {})
      .user(ChangeGreetingText.class).systemPublish(this::greetingTextChanged)
      .user(PublishChangeGreetingTextList.class).systemPublish(this::publishChangeGreetingTextList)
      .user(PublishChangeGreetingTextSet.class).systemPublish(this::publishChangeGreetingTextSet)
      .on(TestFailingUpdateAggregateRootCommand.class).systemPublish(ev -> new TestFailingUpdateAggregateRootEvent())
      .build();
    return model;
  }
  
  @Override
  public Model internalEventHandlers() {
    Model model = Model.builder()
      .on(GreetingTextChanged.class).systemPublish(gtc -> Greeting.create(aggregateRoot().id, gtc.text))
      .on(TestFailingUpdateAggregateRootEvent.class).systemPublish(ev -> "This should be an aggregate root, so it will fail!")
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
}