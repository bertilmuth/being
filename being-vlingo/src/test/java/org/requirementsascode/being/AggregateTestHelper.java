package org.requirementsascode.being;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;
import io.vlingo.xoom.symbio.Source;

class AggregateTestHelper<STATE> {
	private final MapCommands mapCommands;
	private final MapEvents<STATE> mapEvents;
	private List<Source<?>> events;
	private final Aggregate<STATE> aggregate;

	private AggregateTestHelper(Aggregate<STATE> aggregate) {
		this.aggregate = Objects.requireNonNull(aggregate, "aggregate must be non-null!");

		this.mapCommands = aggregate.mapCommands();
		this.mapEvents = aggregate.mapEvents();

		clearEvents();
		createInitialStateOf(aggregate);
	}

	public static <T> AggregateTestHelper<T> of(Aggregate<T> entity) {
		return new AggregateTestHelper<>(entity);
	}

	public AggregateTestHelper<STATE> givenEvents(IdentifiedDomainEvent... internalEvents) {
		Arrays.stream(internalEvents).forEach(mapEvents()::apply);
		clearEvents();
		return this;
	}

	public AggregateTestHelper<STATE> when(Object command) {
		List<? extends IdentifiedDomainEvent> producedEvents = mapCommands().apply(command);
		producedEvents().addAll(producedEvents);

		Optional<STATE> lastState = producedEvents.stream().map(e -> mapEvents().apply(e)).filter(Optional::isPresent)
				.map(state -> state.get()).reduce((first, second) -> second);

		lastState.ifPresent(aggregate::setState);
		return this;
	}

	private void createInitialStateOf(Aggregate<STATE> entity) {
		STATE initialState = entity.initialState(randomId());
		entity.setState(initialState);
	}

	private String randomId() {
		return UUID.randomUUID().toString();
	}

	private MapCommands mapCommands() {
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
