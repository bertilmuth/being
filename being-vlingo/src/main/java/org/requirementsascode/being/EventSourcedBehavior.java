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

public class EventSourcedBehavior<STATE> extends EventSourced implements Behavior<STATE> {
	private final Aggregate<STATE> aggregate;
	private final MapCommands mapCommands;
	private final MapEvents<STATE> mapEvents;

	public EventSourcedBehavior(String aggregateId, Aggregate<STATE> aggregate) {
		super(aggregateId);
		this.aggregate = requireNonNull(aggregate, "aggregate must be non-null");	    
		this.mapCommands = aggregate.mapCommands();
		this.mapEvents = aggregate.mapEvents();

	    initializeAggregate(aggregateId, aggregate);
		registerEventMappers();
	}

	private void registerEventMappers() {
		aggregate.mapEvents().getEventClasses().stream()
			.forEach(clazz -> registerEventMapperFor(clazz));		
	}

	private void registerEventMapperFor(Class<? extends IdentifiedDomainEvent> eventClass) {
		EventSourced.registerConsumer(EventSourcedBehavior.class, eventClass, (b, ev) -> {
			Optional<STATE> updatedState = mapEvents.apply(ev);
			updatedState.ifPresent(aggregate::setState);
		});
	}

	private void initializeAggregate(String aggregateId, Aggregate<STATE> aggregate) {
		STATE state = aggregate.initialState(aggregateId);
	    aggregate.setState(state);
	}

	public Completes<STATE> reactTo(Object command){
		List<? extends IdentifiedDomainEvent> identifiedDomainEvents = mapCommands.apply(command);
		if(identifiedDomainEvents.isEmpty()) {
			throw new RuntimeException("Command handler didn't create event for command: " + command);
		}

		List<Source<DomainEvent>> sourceList = new ArrayList<>(identifiedDomainEvents.size());
		sourceList.addAll(identifiedDomainEvents);
		
		return apply(sourceList, () -> aggregate.state());
	}
}