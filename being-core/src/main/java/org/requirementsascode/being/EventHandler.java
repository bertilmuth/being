package org.requirementsascode.being;

import static java.util.Objects.requireNonNull;

import java.util.function.BiFunction;

import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;

/**
 * Create an instance of this class to define a handler for a specific event
 * type. An event handler transforms an event and a state into a new state
 * instance.
 * 
 * @author b_muth
 *
 * @param <STATE> the type of state passed in as input to the handlers, and
 *                returned as output
 */
public class EventHandler<EVENT extends IdentifiedDomainEvent, STATE> {
	private final Class<EVENT> eventClass;
	private final BiFunction<EVENT, STATE, STATE> eventHandler;

	/**
	 * Define the specific event type handled by this handler.
	 * 
	 * @param <EVENT> the specific event type handled by this handler
	 * @param eventClass the class of the event type handled by this handler
	 * @return a builder for the event handler
	 */
	public static <EVENT extends IdentifiedDomainEvent> EventsOf<EVENT> eventsOf(final Class<EVENT> eventClass) {
		return new EventsOf<EVENT>(eventClass);
	}

	/**
	 * Reacts to the specified event by transforming the event and state into a new
	 * state.
	 * 
	 * @param event   the event to handle
	 * @param <STATE> the type of state passed in as input to the handlers, and
	 *                returned as output
	 * @return the new state instance
	 */
	@SuppressWarnings("unchecked")
	STATE reactTo(final IdentifiedDomainEvent event, final STATE state) {
		return eventHandler.apply((EVENT) event, state);
	}

	Class<EVENT> eventClass() {
		return eventClass;
	}

	public static class EventsOf<EVENT extends IdentifiedDomainEvent> {
		private final Class<EVENT> eventClass;

		private EventsOf(final Class<EVENT> eventClass) {
			this.eventClass = eventClass;
		}

		/**
		 * Define an event handling function that consumes the event and state, and
		 * produces a new state instance.
		 * 
		 * @param <STATE>      the type of the state passed in as input to the handlers,
		 *                     and returned as output
		 * @param eventHandler the event handling function
		 * @return the event handler instance
		 */
		public <STATE> EventHandler<EVENT, STATE> with(final BiFunction<EVENT, STATE, STATE> eventHandler) {
			return new EventHandler<>(eventClass, eventHandler);
		}
	}

	private EventHandler(final Class<EVENT> eventClass, final BiFunction<EVENT, STATE, STATE> eventHandler) {
		this.eventClass = requireNonNull(eventClass, "eventClass must be non-null!");
		this.eventHandler = requireNonNull(eventHandler, "eventHandler must be non-null!");
	}
}
