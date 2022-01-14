package org.requirementsascode.being;

import static java.util.Objects.requireNonNull;

import java.util.function.BiFunction;

import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;

public class EventHandler<EVENT extends IdentifiedDomainEvent, STATE> {
	private final Class<EVENT> eventClass;
	private final BiFunction<EVENT, STATE, STATE> eventHandler;

	public static <EVENT extends IdentifiedDomainEvent> EventsOf<EVENT> eventsOf(final Class<EVENT> eventClass) {
		return new EventsOf<EVENT>(eventClass);
	}

	@SuppressWarnings("unchecked")
	STATE reactTo(final IdentifiedDomainEvent event, final STATE state) {
		return eventHandler.apply((EVENT) event, state);
	}

	public Class<EVENT> eventClass() {
		return eventClass;
	}

	public static class EventsOf<EVENT extends IdentifiedDomainEvent> {
		private final Class<EVENT> eventClass;

		private EventsOf(final Class<EVENT> eventClass) {
			this.eventClass = eventClass;
		}

		public <STATE> EventHandler<EVENT, STATE> with(final BiFunction<EVENT, STATE, STATE> eventHandler) {
			return new EventHandler<>(eventClass, eventHandler);
		}
	}

	private EventHandler(final Class<EVENT> eventClass, final BiFunction<EVENT, STATE, STATE> eventHandler) {
		this.eventClass = requireNonNull(eventClass, "eventClass must be non-null!");
		this.eventHandler = requireNonNull(eventHandler, "eventHandler must be non-null!");
	}
}
