package org.requirementsascode.being;

import java.util.Arrays;
import java.util.List;
import static java.util.Objects.*;
import java.util.UUID;

import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;

public class AggregateTest<CMD,STATE> implements EventApplier<STATE>{
	private final CommandHandlers<CMD,STATE> commandHandlers;
	private final EventHandlers<STATE> eventHandlers;
	private EventConsumer<STATE> eventConsumer;
	private final STATE initialState;

	private STATE state;

	public static <CMD,STATE> AggregateTest<CMD,STATE> of(AggregateBehavior<CMD,STATE> aggregateBehavior) {
		return new AggregateTest<>(aggregateBehavior);
	}
	
	private AggregateTest(AggregateBehavior<CMD,STATE> aggregateBehavior) {
		requireNonNull(aggregateBehavior, "aggregate must be non-null!");

		this.commandHandlers = requireNonNull(aggregateBehavior.commandHandlers(), "commandHandlers(...) must return non-null value!");
		this.eventHandlers = requireNonNull(aggregateBehavior.eventHandlers(), "eventHandlers(...) must return non-null value!");
		this.eventConsumer = new EventConsumer<>(this);
		this.initialState = requireNonNull(aggregateBehavior.initialState(randomId()), "initialState(...) must return non-null value!");

		setInitialState();
	}
	
	public AggregateTest<CMD,STATE> givenEvents(final IdentifiedDomainEvent... events) {
		requireNonNull(events, "events must be non-null!");
		
		setInitialState();
		Arrays.stream(events).forEach(eventConsumer()::consumeEvent);
		return this;
	}

	public AggregateTest<CMD,STATE> when(final CMD command) {
		requireNonNull(command, "events must be non-null!");

		final List<? extends IdentifiedDomainEvent> producedEvents = commandHandlers().reactTo(command,state());
		producedEvents.stream().forEach(eventConsumer()::consumeEvent);

		return this;
	}
	
	public STATE state() {
		return state;
	}

	public void setState(STATE state) {
		requireNonNull(state, "state must be non-null!");

		this.state = state;
	}
	
	public CommandHandlers<CMD,STATE> commandHandlers() {
		return commandHandlers;
	}
	
	public EventHandlers<STATE> eventHandlers() {
		return eventHandlers;
	}

	private String randomId() {
		return UUID.randomUUID().toString();
	}

	private EventConsumer<STATE> eventConsumer() {
		return eventConsumer;
	}
	
	private void setInitialState() {
		setState(initialState);
	}
}
