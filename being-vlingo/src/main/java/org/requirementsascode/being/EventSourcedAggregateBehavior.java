package org.requirementsascode.being;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.vlingo.xoom.actors.Logger;
import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.lattice.model.DomainEvent;
import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;
import io.vlingo.xoom.lattice.model.sourcing.EventSourced;
import io.vlingo.xoom.symbio.Source;

public class EventSourcedAggregateBehavior<STATE> extends EventSourced implements Behavior<STATE> {
	private final EventSourcedAggregate<STATE> aggregate;
	private final MapCommands mapCommands;
	private final MapEvents<STATE> mapEvents;
	private final Logger logger;

	public EventSourcedAggregateBehavior(String aggregateId, EventSourcedAggregate<STATE> aggregate) {
		super(aggregateId);
		this.aggregate = requireNonNull(aggregate, "aggregate must be non-null");	    
		this.mapCommands = aggregate.mapCommands();
		this.mapEvents = aggregate.mapEvents();
		this.logger = super.stage().world().defaultLogger();

	    initializeAggregate(aggregateId, aggregate);
		registerEventMappers();
	}

	private void registerEventMappers() {
		aggregate.mapEvents().getEventClasses().stream()
			.forEach(clazz -> registerEventMapperFor(clazz));		
	}

	private void registerEventMapperFor(Class<? extends IdentifiedDomainEvent> eventClass) {
		EventSourced.registerConsumer(EventSourcedAggregateBehavior.class, eventClass, (b, ev) -> {
			Optional<STATE> updatedState = mapEvents.apply(ev);
			logger.info("Applied event: " + ev);

			updatedState.ifPresent(state -> {
				aggregate.setState(state);
				logger.info("Updated state to: " + state);
			});
		});
	}

	private void initializeAggregate(String aggregateId, EventSourcedAggregate<STATE> aggregate) {
		STATE state = aggregate.initialState(aggregateId);
	    aggregate.setState(state);
	    logger.info("Initialized aggregate: " + aggregateId);
	}

	public Completes<STATE> reactTo(Object command){
		List<? extends IdentifiedDomainEvent> identifiedDomainEvents = mapCommands.apply(command);
	    logger.info("Handled command: " + command + " -> Events:" + identifiedDomainEvents);

		if(identifiedDomainEvents.isEmpty()) {
			throw new RuntimeException("Command handler didn't create event for command: " + command);
		}

		List<Source<DomainEvent>> sourceList = new ArrayList<>(identifiedDomainEvents.size());
		sourceList.addAll(identifiedDomainEvents);
		
		return apply(sourceList, () -> aggregate.state());
	}
}