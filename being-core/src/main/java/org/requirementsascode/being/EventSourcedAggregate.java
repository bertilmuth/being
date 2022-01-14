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

public class EventSourcedAggregate<CMD, STATE> extends EventSourced implements Aggregate<CMD, STATE>, EventApplier<STATE> {
	private final AggregateBehavior<CMD, STATE> aggregate;
	private final CommandHandlers<CMD,STATE> commandHandlers;
	private final EventHandlers<STATE> eventHandlers;
	private final EventConsumer<STATE> eventConsumer;

	private STATE state;

	public EventSourcedAggregate(String aggregateId, Supplier<AggregateBehavior<CMD, STATE>> behaviorSupplier) {
		super(aggregateId);
		requireNonNull(behaviorSupplier, "behaviorSupplier must be non-null");
		this.aggregate = requireNonNull(behaviorSupplier.get(), "supplied aggregate must be non-null!");
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

	private void registerConsumerFor(final Class<? extends IdentifiedDomainEvent> eventClass) {
		EventSourced.registerConsumer(EventSourcedAggregate.class, eventClass,
				(behavior, event) -> behavior.eventConsumer().consumeEvent(event));
	}

	public EventHandlers<STATE> eventHandlers() {
		return eventHandlers;
	}
	
	public STATE state() {
		return state;
	}
	
	public void setState(final STATE state) {
		this.state = state;
	}

	private void initializeAggregate(final String aggregateId, final AggregateBehavior<CMD, STATE> aggregate) {
	    setState(aggregate.initialState(aggregateId));
	}

	public Completes<STATE> reactTo(final CMD command){		
		List<? extends IdentifiedDomainEvent> identifiedDomainEvents = commandHandlers().reactTo(command,state());

		List<Source<DomainEvent>> sourceList = new ArrayList<>(identifiedDomainEvents.size());
		sourceList.addAll(identifiedDomainEvents);
		
		return apply(sourceList, () -> state());
	}

	private AggregateBehavior<CMD, STATE> aggregate() {
		return aggregate;
	}

	private CommandHandlers<CMD, STATE> commandHandlers() {
		return commandHandlers;
	}

	private EventConsumer<STATE> eventConsumer() {
		return eventConsumer;
	}
}