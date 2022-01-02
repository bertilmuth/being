package org.requirementsascode.being;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;

class BehaviorTestHelper<STATE> {
  private final CommandHandlers reactingCommandHandlers;
  private final EventHandlers<STATE> reactingEventHandlers;
  private List<Object> events;
  private final AggregateBehavior<STATE> aggregateBehavior;

  private BehaviorTestHelper(AggregateBehavior<STATE> aggregateBehavior) {
    this.reactingCommandHandlers = aggregateBehavior.commandHandlers();
    this.reactingEventHandlers = aggregateBehavior.eventHandlers();
    this.aggregateBehavior = aggregateBehavior;
    
    clearEvents();   
    createInitialStateOf(aggregateBehavior);
  }

  public static <T> BehaviorTestHelper<T> of(AggregateBehavior<T> aggregateBehavior) {
    return new BehaviorTestHelper<>(aggregateBehavior);
  }
  
  public BehaviorTestHelper<STATE> givenEvents(IdentifiedDomainEvent... internalEvents) {
    Arrays.stream(internalEvents).forEach(reactingEventHandlers()::reactTo);
    clearEvents();
    return this;
  }
  
  public BehaviorTestHelper<STATE> when(Object message){
    reactingCommandHandlers()
      .reactTo(message)
      .ifPresent(publishedEvent -> {
        events().addAll(toEventList(publishedEvent));
        Optional<STATE> optionalState = reactingEventHandlers().reactTo(publishedEvent);
        optionalState.ifPresent(aggregateBehavior::setState);
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
  
  private void createInitialStateOf(AggregateBehavior<STATE> aggregateBehavior){
    requireNonNull(aggregateBehavior, "aggregateBehavior must be non-null");
    STATE initialState = aggregateBehavior.initialState(randomId());
    aggregateBehavior.setState(initialState);
  }

  private String randomId() {
    return UUID.randomUUID().toString();
  }
  
  private CommandHandlers reactingCommandHandlers() {
    return reactingCommandHandlers;
  }

  private EventHandlers<STATE> reactingEventHandlers() {
    return reactingEventHandlers;
  }

  public List<Object> events() {
    return events;
  }
  private void clearEvents() {
    this.events = new ArrayList<>();
  }
}
