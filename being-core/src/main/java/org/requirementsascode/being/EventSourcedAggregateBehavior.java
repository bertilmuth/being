package org.requirementsascode.being;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.lattice.model.DomainEvent;
import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;
import io.vlingo.xoom.lattice.model.sourcing.EventSourced;
import io.vlingo.xoom.symbio.Source;

public class EventSourcedAggregateBehavior<CMD, STATE> extends EventSourced implements AggregateBehavior<CMD, STATE>, EventApplier<STATE> {
	private final EventSourcedAggregate<CMD, STATE> aggregate;
	private final CommandHandlers<CMD,STATE> commandHandlers;
	private final EventHandlers<STATE> eventHandlers;
	private final EventConsumer<STATE> eventConsumer;

	private STATE state;

	public EventSourcedAggregateBehavior(String aggregateId, Supplier<EventSourcedAggregate<CMD, STATE>> aggregateSupplier) {
		super(aggregateId);
		requireNonNull(aggregateSupplier, "aggregateSupplier must be non-null");
		this.aggregate = requireNonNull(aggregateSupplier.get(), "supplied aggregate must be non-null!");
		this.commandHandlers = requireNonNull(aggregate.commandHandlers(), "command handlers must be non-null!");
		this.eventHandlers = requireNonNull(aggregate.eventHandlers(), "event handlers must be non-null!");
		this.eventConsumer = new EventConsumer<>(this);

	    initializeAggregate(aggregateId, aggregate);
		registerEventConsumers();
	}

	private void registerEventConsumers() {
		aggregate().eventHandlers().eventClasses().stream()
			.forEach(clazz -> registerConsumerFor(clazz));		
	}

	private void registerConsumerFor(Class<? extends IdentifiedDomainEvent> eventClass) {
		EventSourced.registerConsumer(EventSourcedAggregateBehavior.class, eventClass,
				(behavior, event) -> behavior.eventConsumer().consumeEvent(event));
	}

	public EventHandlers<STATE> eventHandlers() {
		return eventHandlers;
	}
	
	public STATE state() {
		return state;
	}
	
	public void setState(STATE state) {
		this.state = state;
	}

	private void initializeAggregate(String aggregateId, EventSourcedAggregate<CMD, STATE> aggregate) {
	    setState(aggregate.initialState(aggregateId));
	    logger().info("Initialized aggregate: " + aggregateId + " -> " + aggregate);
	}

	public Completes<STATE> reactTo(CMD command){
		List<? extends IdentifiedDomainEvent> identifiedDomainEvents = commandHandlers().reactTo(command,state());
	    logger().info("Handled command: " + command + " -> Events:" + identifiedDomainEvents);

		List<Source<DomainEvent>> sourceList = new ArrayList<>(identifiedDomainEvents.size());
		sourceList.addAll(identifiedDomainEvents);
		
		return apply(sourceList, () -> state());
	}

	private EventSourcedAggregate<CMD, STATE> aggregate() {
		return aggregate;
	}

	private CommandHandlers<CMD, STATE> commandHandlers() {
		return commandHandlers;
	}

	private EventConsumer<STATE> eventConsumer() {
		return eventConsumer;
	}
}