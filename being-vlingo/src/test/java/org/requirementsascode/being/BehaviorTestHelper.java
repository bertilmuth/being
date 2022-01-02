package org.requirementsascode.being;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

class BehaviorTestHelper<T> {
  private final ReactingCommandHandlers<T> commandHandlers;
  private final ReactingEventHandlers<T> reactingEventHandlers;
  private List<Object> events;

  private BehaviorTestHelper(AggregateBehavior<T> aggregateBehavior) {
    clearEvents();   
    this.commandHandlers = ReactingCommandHandlers.from(aggregateBehavior);
    this.reactingEventHandlers = ReactingEventHandlers.of(aggregateBehavior);
    
    createInitialStateOf(aggregateBehavior);
  }

  public static <T> BehaviorTestHelper<T> of(AggregateBehavior<T> aggregateBehavior) {
    return new BehaviorTestHelper<>(aggregateBehavior);
  }
  
  public BehaviorTestHelper<T> givenEvents(Object... internalEvents) {
    Arrays.stream(internalEvents).forEach(reactingEventHandlers()::reactTo);
    clearEvents();
    return this;
  }
  
  public BehaviorTestHelper<T> when(Object message){
    commandHandlers()
      .reactTo(message)
      .ifPresent(publishedEvent -> {
        events().addAll(toEventList(publishedEvent));
        reactingEventHandlers().reactTo(publishedEvent);
      });

    return this;
  }

  private List<Object> toEventList(Object ev) {
    List<Object> eventList = null;
    if(ev instanceof Collection) {
      eventList = new ArrayList<Object>((Collection<?>)ev);
    } else {
      eventList = Arrays.asList(ev);
    }
    return eventList;
  }
  
  private void createInitialStateOf(AggregateBehavior<T> aggregateBehavior){
    requireNonNull(aggregateBehavior, "aggregateBehavior must be non-null");
    T initialState = aggregateBehavior.initialState(randomId());
    aggregateBehavior.setState(initialState);
  }

  private String randomId() {
    return UUID.randomUUID().toString();
  }
  
  private ReactingCommandHandlers<T> commandHandlers() {
    return commandHandlers;
  }

  private ReactingEventHandlers<T> reactingEventHandlers() {
    return reactingEventHandlers;
  }

  public List<Object> events() {
    return events;
  }
  private void clearEvents() {
    this.events = new ArrayList<>();
  }
}
