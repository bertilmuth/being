package org.requirementsascode.being;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;

public class MapEvents<STATE> {
	private final List<MapEvent<? extends IdentifiedDomainEvent, STATE>> mapEvents;

	@SafeVarargs
	public static <STATE> MapEvents<STATE> with(MapEvent<? extends IdentifiedDomainEvent, STATE>... mapEvents) {
		return new MapEvents<>(mapEvents);
	}
	
	@SafeVarargs
	private MapEvents(MapEvent<? extends IdentifiedDomainEvent, STATE>... mapEvents) {
		Objects.requireNonNull(mapEvents, "mapEvents must be non-null!");
		this.mapEvents = Arrays.asList(mapEvents);
	}
	
	public Optional<STATE> reactTo(IdentifiedDomainEvent event) {
		Class<? extends IdentifiedDomainEvent> eventClass = Objects.requireNonNull(event, "event must be non-null!").getClass();
		
		Optional<STATE> optionalState = mapEvents.stream()
			.filter(h -> h.getEventClass().equals(eventClass))
			.map(h -> h.reactTo(event))
			.findFirst();
		
		return optionalState;
	}

	public List<Class<? extends IdentifiedDomainEvent>> getEventClasses() {
		final List<Class<? extends IdentifiedDomainEvent>> eventClasses = 
			mapEvents.stream()
			.map(MapEvent::getEventClass)
			.collect(Collectors.toList());
		return eventClasses;
	}
}
