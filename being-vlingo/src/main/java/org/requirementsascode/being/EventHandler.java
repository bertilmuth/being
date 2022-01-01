package org.requirementsascode.being;

import java.util.Objects;
import java.util.function.Function;

import io.vlingo.xoom.symbio.Source;

public class EventHandler<T extends Source<?>> {
	private final Class<T> eventClass;
	private final Function<T, ?> handler;

	public static <T extends Source<?>> EventHandler<T> on(Class<T> eventClass, Function<T, ?> handler) {
		return new EventHandler<>(eventClass, handler);
	}
	
	private EventHandler(Class<T> eventClass, Function<T, ?> handler) {
		this.eventClass = Objects.requireNonNull(eventClass, "eventClass must be non-null!");
		this.handler = Objects.requireNonNull(handler, "handler must be non-null!");
	}

	public Class<T> getEventClass() {
		return eventClass;
	}

	public Function<T, ?> getHandler() {
		return handler;
	}
}
