package org.requirementsascode.being;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;

public class MapEvents<STATE> implements Function<IdentifiedDomainEvent, Optional<STATE>>{
	private final List<MapEvent<? extends IdentifiedDomainEvent, STATE>> mapEvent;

	@SafeVarargs
	public static <STATE> MapEvents<STATE> with(MapEvent<? extends IdentifiedDomainEvent, STATE>... mapEvent) {
		return new MapEvents<>(mapEvent);
	}
	
	@SafeVarargs
	private MapEvents(MapEvent<? extends IdentifiedDomainEvent, STATE>... mapEvent) {
		Objects.requireNonNull(mapEvent, "mapEvent must be non-null!");
		this.mapEvent = Arrays.asList(mapEvent);
	}
	
	@Override
	public Optional<STATE> apply(IdentifiedDomainEvent event) {
		Class<? extends IdentifiedDomainEvent> eventClass = Objects.requireNonNull(event, "event must be non-null!").getClass();
		
		Optional<STATE> optionalState = mapEvent.stream()
			.filter(mapEvent -> mapEvent.getEventClass().equals(eventClass))
			.map(mapEvent -> mapEvent.map(event))
			.findFirst();
		
		return optionalState;
	}

	public List<Class<? extends IdentifiedDomainEvent>> getEventClasses() {
		final List<Class<? extends IdentifiedDomainEvent>> eventClasses = 
			mapEvent.stream()
			.map(MapEvent::getEventClass)
			.collect(Collectors.toList());
		return eventClasses;
	}
}
