package org.requirementsascode.being;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;

public class MapEvents<STATE> implements Function<IdentifiedDomainEvent, Optional<STATE>>{
	private final List<MapEvent<? extends IdentifiedDomainEvent, STATE>> eventMappers;

	@SafeVarargs
	public static <STATE> MapEvents<STATE> with(MapEvent<? extends IdentifiedDomainEvent, STATE>... eventMappers) {
		return new MapEvents<>(eventMappers);
	}
	
	@SafeVarargs
	private MapEvents(MapEvent<? extends IdentifiedDomainEvent, STATE>... eventMappers) {
		Objects.requireNonNull(eventMappers, "eventMappers must be non-null!");
		this.eventMappers = Arrays.asList(eventMappers);
	}
	
	@Override
	public Optional<STATE> apply(IdentifiedDomainEvent event) {
		Class<? extends IdentifiedDomainEvent> eventClass = Objects.requireNonNull(event, "event must be non-null!").getClass();
		
		Optional<STATE> optionalState = eventMappers.stream()
			.filter(h -> h.getEventClass().equals(eventClass))
			.map(h -> h.apply(event))
			.findFirst();
		
		return optionalState;
	}

	public List<Class<? extends IdentifiedDomainEvent>> getEventClasses() {
		final List<Class<? extends IdentifiedDomainEvent>> eventClasses = 
			eventMappers.stream()
			.map(MapEvent::getEventClass)
			.collect(Collectors.toList());
		return eventClasses;
	}
}
