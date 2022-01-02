package org.requirementsascode.being;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;
import io.vlingo.xoom.symbio.Source;

public class EventHandlers<STATE> {
	private final List<EventHandler<? extends Source<?>, STATE>> eventHandlers;

	@SafeVarargs
	public static <STATE> EventHandlers<STATE> are(EventHandler<? extends Source<?>, STATE>... eventHandlers) {
		return new EventHandlers<>(eventHandlers);
	}
	
	@SafeVarargs
	private EventHandlers(EventHandler<? extends Source<?>, STATE>... eventHandlers) {
		Objects.requireNonNull(eventHandlers, "eventHandlers must be non-null!");
		this.eventHandlers = Arrays.asList(eventHandlers);
	}
	
	public Optional<STATE> reactTo(IdentifiedDomainEvent event) {
		Class<? extends IdentifiedDomainEvent> eventClass = Objects.requireNonNull(event, "event must be non-null!").getClass();
		
		Optional<STATE> optionalState = eventHandlers.stream()
			.filter(h -> h.getEventClass().equals(eventClass))
			.map(h -> h.reactTo(event))
			.findFirst();
		
		return optionalState;
	}

	public List<Class<? extends IdentifiedDomainEvent>> getEventClasses() {
		final List<Class<? extends IdentifiedDomainEvent>> eventClasses = 
			eventHandlers.stream()
			.map(EventHandler::getEventClass)
			.collect(Collectors.toList());
		return eventClasses;
	}

	public List<Function<? extends Source<?>, ?>> getHandlers() {
		List<Function<? extends Source<?>, ?>> handlers = 
			eventHandlers.stream()
			.map(EventHandler::getHandler)
			.collect(Collectors.toList());
		return handlers;
	}
}
