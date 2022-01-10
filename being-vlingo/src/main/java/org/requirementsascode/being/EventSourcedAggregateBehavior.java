package org.requirementsascode.being;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import io.vlingo.xoom.actors.Logger;
import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.lattice.model.DomainEvent;
import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;
import io.vlingo.xoom.lattice.model.sourcing.EventSourced;
import io.vlingo.xoom.symbio.Source;

public class EventSourcedAggregateBehavior<CMD, STATE> extends EventSourced implements AggregateBehavior<CMD, STATE> {
	private final EventSourcedAggregate<CMD, STATE> aggregate;
	private final CommandHandlers<CMD,STATE> commandHandlers;
	private final EventHandlers<STATE> eventHandlers;
	private final Logger logger;
	
	private STATE state;

	public EventSourcedAggregateBehavior(String aggregateId, Supplier<EventSourcedAggregate<CMD, STATE>> aggregateSupplier) {
		super(aggregateId);
		requireNonNull(aggregateSupplier, "aggregateSupplier must be non-null");
		this.aggregate = requireNonNull(aggregateSupplier.get(), "supplied aggregate must be non-null!");
		this.commandHandlers = requireNonNull(aggregate.commandHandlers(), "command handlers must be non-null!");
		this.eventHandlers = requireNonNull(aggregate.eventHandlers(), "event handlers must be non-null!");
		this.logger = super.stage().world().defaultLogger();

	    initializeAggregate(aggregateId, aggregate);
		registerEventConsumers();
	}

	private void registerEventConsumers() {
		aggregate.eventHandlers().getEventClasses().stream()
			.forEach(clazz -> registerConsumerFor(clazz));		
	}

	private void registerConsumerFor(Class<? extends IdentifiedDomainEvent> eventClass) {
		EventSourced.registerConsumer(EventSourcedAggregateBehavior.class, eventClass, EventSourcedAggregateBehavior::consumeEvent);
	}
	
	private static <CMD, STATE> void consumeEvent(EventSourcedAggregateBehavior<CMD, STATE> aggregateBehavior, IdentifiedDomainEvent event) {
		Optional<STATE> updatedState = aggregateBehavior.reactToEvent(event);
		aggregateBehavior.logInfo("Applied event: " + event);

		updatedState.ifPresent(state -> {
			aggregateBehavior.setState(state);
			aggregateBehavior.logInfo("Updated state to: " + state);
		});
	}
	
	private Optional<STATE> reactToEvent(IdentifiedDomainEvent event){
		return eventHandlers.reactTo(event, state());
	}
	
	private STATE state() {
		return state;
	}
	
	private void setState(STATE state) {
		this.state = state;
	}
	
	private void logInfo(String text) {
		logger.info(text);
	}

	private void initializeAggregate(String aggregateId, EventSourcedAggregate<CMD, STATE> aggregate) {
		STATE state = aggregate.initialState(aggregateId);
	    setState(state);
	    logger.info("Initialized aggregate: " + aggregateId + " -> " + aggregate);
	}

	public Completes<STATE> reactTo(CMD command){
		List<? extends IdentifiedDomainEvent> identifiedDomainEvents = commandHandlers.reactTo(command,state());
	    logger.info("Handled command: " + command + " -> Events:" + identifiedDomainEvents);

		if(identifiedDomainEvents.isEmpty()) {
			throw new RuntimeException("Command handler didn't create event for command: " + command);
		}

		List<Source<DomainEvent>> sourceList = new ArrayList<>(identifiedDomainEvents.size());
		sourceList.addAll(identifiedDomainEvents);
		
		return apply(sourceList, () -> {
			logInfo("State returned to user: " + state());
			return state();
		});
	}
	
}