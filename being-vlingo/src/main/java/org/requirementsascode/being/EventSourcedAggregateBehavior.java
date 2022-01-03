package org.requirementsascode.being;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.lattice.model.DomainEvent;
import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;
import io.vlingo.xoom.lattice.model.sourcing.EventSourced;
import io.vlingo.xoom.symbio.Source;

public class EventSourcedAggregateBehavior<STATE> extends EventSourced implements CompletableBehavior<STATE> {
	private final AggregateBehavior<STATE> aggregateBehavior;
	private final MapCommands mapCommands;
	private final MapEvents<STATE> mapEvents;

	public EventSourcedAggregateBehavior(String entityId, AggregateBehavior<STATE> aggregateBehavior) {
		super(entityId);
		this.aggregateBehavior = requireNonNull(aggregateBehavior, "aggregateBehavior must be non-null");	    
		this.mapCommands = aggregateBehavior.mapCommands();
		this.mapEvents = aggregateBehavior.mapEvents();

	    createAggregate(entityId, aggregateBehavior);
		
		registerEventMappers();
	}

	private void registerEventMappers() {
		aggregateBehavior.mapEvents().getEventClasses().stream()
			.forEach(clazz -> registerEventMapperFor(clazz));		
	}

	private void registerEventMapperFor(Class<? extends IdentifiedDomainEvent> eventClass) {
		EventSourced.registerConsumer(EventSourcedAggregateBehavior.class, eventClass, (b, ev) -> {
			Optional<STATE> updatedState = mapEvents.apply(ev);
			updatedState.ifPresent(aggregateBehavior::setState);
		});
	}

	private void createAggregate(String entityId, AggregateBehavior<STATE> aggregateBehavior) {
		STATE state = aggregateBehavior.initialState(entityId);
	    aggregateBehavior.setState(state);
	}

	public Completes<STATE> reactTo(Object command){
		List<? extends IdentifiedDomainEvent> identifiedDomainEvents = mapCommands.apply(command);
		if(identifiedDomainEvents.isEmpty()) {
			throw new RuntimeException("Command handler didn't create event for command: " + command);
		}

		List<Source<DomainEvent>> sourceList = new ArrayList<>(identifiedDomainEvents.size());
		sourceList.addAll(identifiedDomainEvents);
		
		return apply(sourceList, () -> aggregateBehavior.state());
	}
}