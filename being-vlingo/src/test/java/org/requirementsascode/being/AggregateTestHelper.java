package org.requirementsascode.being;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;
import io.vlingo.xoom.symbio.Source;

class AggregateTestHelper<CMD,STATE> {
	private final CommandHandlers<CMD,STATE> commandHandlers;
	private final EventHandlers<STATE> eventHandlers;
	private List<Source<?>> events;
	
	private STATE state;

	private AggregateTestHelper(EventSourcedAggregate<CMD,STATE> aggregate) {
		Objects.requireNonNull(aggregate, "aggregate must be non-null!");

		this.commandHandlers = aggregate.commandHandlers();
		this.eventHandlers = aggregate.eventHandlers();

		clearEvents();
		createInitialStateOf(aggregate);
	}
	
	private void createInitialStateOf(EventSourcedAggregate<CMD,STATE> aggregate) {
		STATE initialState = aggregate.initialState(randomId());
		setState(initialState);
	}
	
	public STATE state() {
		return state;
	}

	private void setState(STATE state) {
		this.state = state;
	}

	public static <CMD,STATE> AggregateTestHelper<CMD,STATE> of(EventSourcedAggregate<CMD,STATE> aggregate) {
		return new AggregateTestHelper<>(aggregate);
	}

	public AggregateTestHelper<CMD,STATE> givenEvents(IdentifiedDomainEvent... internalEvents) {
		Arrays.stream(internalEvents).forEach(ev -> eventHandlers().reactTo(ev, state()));
		clearEvents();
		return this;
	}

	public AggregateTestHelper<CMD,STATE> when(CMD command) {
		List<? extends IdentifiedDomainEvent> producedEvents = commandHandlers().reactTo(command,state());
		producedEvents().addAll(producedEvents);

		Optional<STATE> lastState = producedEvents.stream().map(ev -> eventHandlers().reactTo(ev, state())).filter(Optional::isPresent)
				.map(state -> state.get()).reduce((first, second) -> second);

		lastState.ifPresent(this::setState);
		return this;
	}

	private String randomId() {
		return UUID.randomUUID().toString();
	}

	private CommandHandlers<CMD,STATE> commandHandlers() {
		return commandHandlers;
	}

	private EventHandlers<STATE> eventHandlers() {
		return eventHandlers;
	}

	public List<Source<?>> producedEvents() {
		return events;
	}

	private void clearEvents() {
		this.events = new ArrayList<>();
	}
}
