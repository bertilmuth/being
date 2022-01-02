package org.requirementsascode.being;

import java.util.Objects;
import java.util.function.Function;

import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;

public class EventHandler<EVENT extends IdentifiedDomainEvent, STATE> {
	private final Class<EVENT> eventClass;
	private final Function<EVENT, STATE> handler;

	public static <EVENT extends IdentifiedDomainEvent, STATE> EventHandler<EVENT, STATE> eventsOf(Class<EVENT> eventClass, Function<EVENT, STATE> handler) {
		return new EventHandler<>(eventClass, handler);
	}
	
	private EventHandler(Class<EVENT> eventClass, Function<EVENT, STATE> handler) {
		this.eventClass = Objects.requireNonNull(eventClass, "eventClass must be non-null!");
		this.handler = Objects.requireNonNull(handler, "handler must be non-null!");
	}

	public Class<EVENT> getEventClass() {
		return eventClass;
	}

	public Function<EVENT, STATE> getHandler() {
		return handler;
	}
	
	@SuppressWarnings("unchecked")
	STATE reactTo(IdentifiedDomainEvent event) {
		return handler.apply((EVENT)event);
	}
}
