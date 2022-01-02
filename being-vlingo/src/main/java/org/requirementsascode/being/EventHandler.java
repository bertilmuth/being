package org.requirementsascode.being;

import java.util.Objects;
import java.util.function.Function;

import io.vlingo.xoom.symbio.Source;

public class EventHandler<EVENT extends Source<?>, STATE> {
	private final Class<EVENT> eventClass;
	private final Function<EVENT, ?> handler;

	public static <EVENT extends Source<?>, STATE> EventHandler<EVENT, STATE> eventHandler(Class<EVENT> eventClass, Function<EVENT, STATE> handler) {
		return new EventHandler<>(eventClass, handler);
	}
	
	private EventHandler(Class<EVENT> eventClass, Function<EVENT, STATE> handler) {
		this.eventClass = Objects.requireNonNull(eventClass, "eventClass must be non-null!");
		this.handler = Objects.requireNonNull(handler, "handler must be non-null!");
	}

	public Class<EVENT> getEventClass() {
		return eventClass;
	}

	public Function<EVENT, ?> getHandler() {
		return handler;
	}
}
