package org.requirementsascode.being;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;
import io.vlingo.xoom.symbio.Source;

class AggregateTest<CMD,STATE> implements EventApplier<STATE>{
	private final CommandHandlers<CMD,STATE> commandHandlers;
	private final EventHandlers<STATE> eventHandlers;
	private List<Source<?>> events;
	private EventConsumer<STATE> eventConsumer;

	private STATE state;

	private AggregateTest(EventSourcedAggregate<CMD,STATE> aggregate) {
		Objects.requireNonNull(aggregate, "aggregate must be non-null!");

		this.commandHandlers = aggregate.commandHandlers();
		this.eventHandlers = aggregate.eventHandlers();
		this.eventConsumer = new EventConsumer<>(this);

		clearEvents();
		createInitialStateOf(aggregate, randomId());
	}
	
	private void createInitialStateOf(EventSourcedAggregate<CMD,STATE> aggregate, String id) {
		STATE initialState = aggregate.initialState(id);
		setState(initialState);
	}
	
	public STATE state() {
		return state;
	}

	public void setState(STATE state) {
		this.state = state;
	}
	
	public EventHandlers<STATE> eventHandlers() {
		return eventHandlers;
	}
	
	public List<Source<?>> producedEvents() {
		return events;
	}

	public static <CMD,STATE> AggregateTest<CMD,STATE> of(EventSourcedAggregate<CMD,STATE> aggregate) {
		return new AggregateTest<>(aggregate);
	}

	public AggregateTest<CMD,STATE> givenEvents(IdentifiedDomainEvent... internalEvents) {
		Arrays.stream(internalEvents).forEach(eventConsumer()::consumeEvent);
		clearEvents();
		return this;
	}

	public AggregateTest<CMD,STATE> when(CMD command) {
		List<? extends IdentifiedDomainEvent> producedEvents = commandHandlers().reactTo(command,state());
		producedEvents().addAll(producedEvents);
		producedEvents.stream().forEach(eventConsumer()::consumeEvent);

		return this;
	}

	private String randomId() {
		return UUID.randomUUID().toString();
	}

	private CommandHandlers<CMD,STATE> commandHandlers() {
		return commandHandlers;
	}

	private void clearEvents() {
		this.events = new ArrayList<>();
	}

	private EventConsumer<STATE> eventConsumer() {
		return eventConsumer;
	}
}
