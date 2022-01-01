package org.requirementsascode.being;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.vlingo.xoom.symbio.Source;

public class EventHandlers {
	private final List<EventHandler<?>> eventHandlers;

	public static EventHandlers are(EventHandler<?>... eventHandlers) {
		return new EventHandlers(eventHandlers);
	}
	
	private EventHandlers(EventHandler<?>... eventHandlers) {
		Objects.requireNonNull(eventHandlers, "eventHandlers must be non-null!");
		this.eventHandlers = Arrays.asList(eventHandlers);
	}

	public List<Class<? extends Source<?>>> getEventClasses() {
		final List<Class<? extends Source<?>>> eventClasses = 
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
