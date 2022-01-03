package org.requirementsascode.being;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;
import io.vlingo.xoom.symbio.Source;

class BehaviorTestHelper<STATE> {
  private final MapCommands mapCommands;
  private final MapEvents<STATE> mapEvents;
  private List<Source<?>> events;
  private final AggregateBehavior<STATE> aggregateBehavior;

  private BehaviorTestHelper(AggregateBehavior<STATE> aggregateBehavior) {
    this.mapCommands = aggregateBehavior.mapCommands();
    this.mapEvents = aggregateBehavior.mapEvents();
    this.aggregateBehavior = aggregateBehavior;
    
    clearEvents();   
    createInitialStateOf(aggregateBehavior);
  }

  public static <T> BehaviorTestHelper<T> of(AggregateBehavior<T> aggregateBehavior) {
    return new BehaviorTestHelper<>(aggregateBehavior);
  }
  
  public BehaviorTestHelper<STATE> givenEvents(IdentifiedDomainEvent... internalEvents) {
    Arrays.stream(internalEvents).forEach(mapEvents()::reactTo);
    clearEvents();
    return this;
  }
  
  public BehaviorTestHelper<STATE> when(Object message){
    List<? extends IdentifiedDomainEvent> newEvents = mapCommands().apply(message);
    events().addAll(newEvents);
    
    Optional<STATE> lastState = newEvents.stream()
    	.map(e -> mapEvents().reactTo(e))
    	.filter(Optional::isPresent)
    	.map(state -> state.get())
    	.reduce((first, second) -> second);
    
    lastState.ifPresent(aggregateBehavior::setState);
    return this;
  }
  
  private void createInitialStateOf(AggregateBehavior<STATE> aggregateBehavior){
    requireNonNull(aggregateBehavior, "aggregateBehavior must be non-null");
    STATE initialState = aggregateBehavior.initialState(randomId());
    aggregateBehavior.setState(initialState);
  }

  private String randomId() {
    return UUID.randomUUID().toString();
  }
  
  private MapCommands mapCommands() {
    return mapCommands;
  }

  private MapEvents<STATE> mapEvents() {
    return mapEvents;
  }

  public List<Source<?>> events() {
    return events;
  }
  private void clearEvents() {
    this.events = new ArrayList<>();
  }
}
