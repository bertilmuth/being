package org.requirementsascode.being;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;



public class EventHandlers<STATE>{
	private final List<EventHandler<? extends IdentifiedDomainEvent, STATE>> eventHandlers;
	
	@SafeVarargs
	public static <STATE> EventHandlers<STATE> handle(EventHandler<? extends IdentifiedDomainEvent, STATE>... eventHandlers) {
		return new EventHandlers<>(eventHandlers);
	}
	
	public Optional<STATE> reactTo(IdentifiedDomainEvent event, STATE state) {
		Class<? extends IdentifiedDomainEvent> eventClass = Objects.requireNonNull(event, "event must be non-null!").getClass();
		Objects.requireNonNull(state, "state must be non-null!");
		
		Optional<STATE> optionalState = eventHandlers().stream()
			.filter(eventHandler -> eventHandler.eventClass().equals(eventClass))
			.map(eventHandler -> eventHandler.reactTo(event, state))
			.findFirst();
		
		return optionalState;
	}
	
	public List<Class<? extends IdentifiedDomainEvent>> eventClasses() {
		final List<Class<? extends IdentifiedDomainEvent>> eventClasses = 
			eventHandlers().stream()
			.map(EventHandler::eventClass)
			.collect(Collectors.toList());
		return eventClasses;
	}
	
	List<EventHandler<? extends IdentifiedDomainEvent, STATE>> eventHandlers(){
		return eventHandlers;
	}
	
	@SafeVarargs
	private EventHandlers(EventHandler<? extends IdentifiedDomainEvent, STATE>... eventHandlers) {
		Objects.requireNonNull(eventHandlers, "eventHandlers must be non-null!");
		this.eventHandlers = Arrays.asList(eventHandlers);
	}

}
