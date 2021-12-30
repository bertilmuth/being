package org.requirementsascode.being;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

class BehaviorTestHelper<T> {
  private final CommandHandlers<T> commandHandlers;
  private final InternalEventHandlers<T> internalEventHandlers;
  private List<Object> internalEvents;

  private BehaviorTestHelper(AggregateBehavior<T> aggregateBehavior) {
    clearInternalEvents();   
    this.commandHandlers = CommandHandlers.fromBehavior(aggregateBehavior);
    this.internalEventHandlers = InternalEventHandlers.fromBehavior(aggregateBehavior);
    
    createInitialAggregateRootFrom(aggregateBehavior);
  }

  public static <T> BehaviorTestHelper<T> of(AggregateBehavior<T> aggregateBehavior) {
    return new BehaviorTestHelper<>(aggregateBehavior);
  }
  
  public BehaviorTestHelper<T> givenEvents(Object... internalEvents) {
    Arrays.stream(internalEvents).forEach(internalEventHandlers()::reactTo);
    clearInternalEvents();
    return this;
  }
  
  public BehaviorTestHelper<T> when(Object message){
    incomingMessageHandlers()
      .reactTo(message)
      .ifPresent(publishedEvent -> {
        internalEvents().addAll(toEventList(publishedEvent));
        internalEventHandlers().reactTo(publishedEvent);
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
  
  private void createInitialAggregateRootFrom(AggregateBehavior<T> aggregateBehavior){
    requireNonNull(aggregateBehavior, "aggregateBehavior must be non-null");
    T aggregateRoot = aggregateBehavior.initialState(randomId());
    aggregateBehavior.setState(aggregateRoot);
  }

  private String randomId() {
    return UUID.randomUUID().toString();
  }
  
  private CommandHandlers<T> incomingMessageHandlers() {
    return commandHandlers;
  }

  private InternalEventHandlers<T> internalEventHandlers() {
    return internalEventHandlers;
  }

  public List<Object> internalEvents() {
    return internalEvents;
  }
  private void clearInternalEvents() {
    this.internalEvents = new ArrayList<>();
  }
}
