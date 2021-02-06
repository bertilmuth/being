package org.requirementsascode.being;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class AggregateBehaviorTest<T> {
  private final CommandHandlers<T> incomingMessageHandlers;
  private final InternalEventHandlers<T> internalEventHandlers;
  private List<Object> internalEvents;

  private AggregateBehaviorTest(AggregateBehavior<T> aggregateBehavior) {
    clearInternalEvents();   
    this.incomingMessageHandlers = CommandHandlers.fromBehavior(aggregateBehavior);
    this.internalEventHandlers = InternalEventHandlers.fromBehavior(aggregateBehavior);
    
    createInitialAggregateRootFrom(aggregateBehavior);
  }

  public static <T> AggregateBehaviorTest<T> of(AggregateBehavior<T> aggregateBehavior) {
    return new AggregateBehaviorTest<>(aggregateBehavior);
  }
  
  public AggregateBehaviorTest<T> givenEvents(Object... internalEvents) {
    Arrays.stream(internalEvents).forEach(internalEventHandlers()::reactTo);
    clearInternalEvents();
    return this;
  }
  
  public AggregateBehaviorTest<T> when(Object message){
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
    T aggregateRoot = aggregateBehavior.createAggregateRoot(randomId());
    aggregateBehavior.setAggregateRoot(aggregateRoot);
  }

  private String randomId() {
    return UUID.randomUUID().toString();
  }
  
  private CommandHandlers<T> incomingMessageHandlers() {
    return incomingMessageHandlers;
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
