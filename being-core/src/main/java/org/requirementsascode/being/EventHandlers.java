package org.requirementsascode.being;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;

/**
 * Create an instance of this class to define which event type is handled by
 * which event handler. An event handler transforms an event and a state into a
 * new state instance.
 * 
 * @author b_muth
 *
 * @param <STATE> the type of the state passed in as input to the handlers, and
 *                returned as output
 */
public class EventHandlers<STATE> {
	private final List<EventHandler<? extends IdentifiedDomainEvent, STATE>> eventHandlers;

	/**
	 * Define the event handlers that handle the persisted events.
	 * 
	 * @param <STATE>       the type of the state passed in as input to the
	 *                      handlers, and returned as output
	 * @param eventHandlers the event handlers
	 * @return an instance of this class
	 */
	@SafeVarargs
	public static <STATE> EventHandlers<STATE> handle(final EventHandler<? extends IdentifiedDomainEvent, STATE>... eventHandlers) {
		return new EventHandlers<>(eventHandlers);
	}

	/**
	 * Reacts to the specified event, i.e. looks if there is an event handler for
	 * the event's type, and if yes, uses it to transform the event and state into a
	 * new state. If there is more than one handler, this method picks the first
	 * one.
	 * 
	 * @param event the event to handle
	 * @param state the state used as input to the event handler
	 * @return the new state instance
	 */
	public Optional<STATE> reactTo(final IdentifiedDomainEvent event, final STATE state) {
		Class<? extends IdentifiedDomainEvent> eventClass = event.getClass();

		Optional<STATE> optionalState = eventHandlers().stream()
			.filter(eventHandler -> eventHandler.eventClass().equals(eventClass))
			.map(eventHandler -> eventHandler.reactTo(event, state)).findFirst();

		return optionalState;
	}

	/**
	 * Returns the classes of events (i.e. the event types) that this instance
	 * handles.
	 * 
	 * @return the event types
	 */
	public List<Class<? extends IdentifiedDomainEvent>> eventClasses() {
		List<Class<? extends IdentifiedDomainEvent>> eventClasses = eventHandlers().stream().map(EventHandler::eventClass)
			.collect(Collectors.toList());
		return eventClasses;
	}

	List<EventHandler<? extends IdentifiedDomainEvent, STATE>> eventHandlers() {
		return eventHandlers;
	}

	@SafeVarargs
	private EventHandlers(final EventHandler<? extends IdentifiedDomainEvent, STATE>... eventHandlers) {
		requireNonNull(eventHandlers, "eventHandlers must be non-null!");
		this.eventHandlers = Arrays.asList(eventHandlers);
	}
}
