package org.requirementsascode.being;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;



class AggregateTest<CMD,STATE> implements EventApplier<STATE>{
	private final CommandHandlers<CMD,STATE> commandHandlers;
	private final EventHandlers<STATE> eventHandlers;
	private EventConsumer<STATE> eventConsumer;
	private final STATE initialState;

	private STATE state;

	public static <CMD,STATE> AggregateTest<CMD,STATE> of(EventSourcedAggregate<CMD,STATE> aggregate) {
		return new AggregateTest<>(aggregate);
	}
	
	private AggregateTest(EventSourcedAggregate<CMD,STATE> aggregate) {
		Objects.requireNonNull(aggregate, "aggregate must be non-null!");

		this.commandHandlers = aggregate.commandHandlers();
		this.eventHandlers = aggregate.eventHandlers();
		this.eventConsumer = new EventConsumer<>(this);
		this.initialState = aggregate.initialState(randomId());

		setInitialState();
	}
	
	public AggregateTest<CMD,STATE> givenEvents(IdentifiedDomainEvent... events) {
		setInitialState();
		Arrays.stream(events).forEach(eventConsumer()::consumeEvent);
		return this;
	}

	public AggregateTest<CMD,STATE> when(CMD command) {
		List<? extends IdentifiedDomainEvent> producedEvents = commandHandlers().reactTo(command,state());
		producedEvents.stream().forEach(eventConsumer()::consumeEvent);

		return this;
	}
	
	public STATE state() {
		return state;
	}

	public void setState(STATE state) {
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
