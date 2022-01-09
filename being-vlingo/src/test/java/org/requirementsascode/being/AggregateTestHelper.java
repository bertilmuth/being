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
	private final MapCommands<CMD> mapCommands;
	private final MapEvents<STATE> mapEvents;
	private List<Source<?>> events;
	private final EventSourcedAggregate<CMD,STATE> aggregate;

	private AggregateTestHelper(EventSourcedAggregate<CMD,STATE> aggregate) {
		this.aggregate = Objects.requireNonNull(aggregate, "aggregate must be non-null!");

		this.mapCommands = aggregate.mapCommands();
		this.mapEvents = aggregate.mapEvents();

		clearEvents();
		createInitialStateOf(aggregate);
	}
	
	private void createInitialStateOf(EventSourcedAggregate<CMD,STATE> aggregate) {
		STATE initialState = aggregate.initialState(randomId());
		aggregate.setState(initialState);
	}

	public static <CMD,STATE> AggregateTestHelper<CMD,STATE> of(EventSourcedAggregate<CMD,STATE> aggregate) {
		return new AggregateTestHelper<>(aggregate);
	}

	public AggregateTestHelper<CMD,STATE> givenEvents(IdentifiedDomainEvent... internalEvents) {
		Arrays.stream(internalEvents).forEach(mapEvents()::apply);
		clearEvents();
		return this;
	}

	public AggregateTestHelper<CMD,STATE> when(CMD command) {
		List<? extends IdentifiedDomainEvent> producedEvents = mapCommands().apply(command);
		producedEvents().addAll(producedEvents);

		Optional<STATE> lastState = producedEvents.stream().map(e -> mapEvents().apply(e)).filter(Optional::isPresent)
				.map(state -> state.get()).reduce((first, second) -> second);

		lastState.ifPresent(aggregate::setState);
		return this;
	}

	private String randomId() {
		return UUID.randomUUID().toString();
	}

	private MapCommands<CMD> mapCommands() {
		return mapCommands;
	}

	private MapEvents<STATE> mapEvents() {
		return mapEvents;
	}

	public List<Source<?>> producedEvents() {
		return events;
	}

	private void clearEvents() {
		this.events = new ArrayList<>();
	}
}
