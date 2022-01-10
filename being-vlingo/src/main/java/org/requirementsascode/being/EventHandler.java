package org.requirementsascode.being;

import java.util.Objects;
import java.util.function.BiFunction;

import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;

public class EventHandler<EVENT extends IdentifiedDomainEvent, STATE>{
	private final Class<EVENT> eventClass;
	private final BiFunction<EVENT, STATE, STATE> eventHandler;
	
	public static <EVENT extends IdentifiedDomainEvent> EventsOf<EVENT> eventsOf(Class<EVENT> eventClass) {
		return new EventsOf<EVENT>(eventClass);
	}
	
	public static class EventsOf<EVENT extends IdentifiedDomainEvent>{
		private final Class<EVENT> eventClass;
		
		private EventsOf(Class<EVENT> eventClass){
			this.eventClass = eventClass;
		}
		
		public <STATE> EventHandler<EVENT, STATE> with(BiFunction<EVENT, STATE, STATE> eventHandler){
			return new EventHandler<>(eventClass, eventHandler);
		}
	}
	
	private EventHandler(Class<EVENT> eventClass, BiFunction<EVENT, STATE, STATE> eventHandler) {
		this.eventClass = Objects.requireNonNull(eventClass, "eventClass must be non-null!");
		this.eventHandler = Objects.requireNonNull(eventHandler, "eventHandler must be non-null!");
	}
	
	@SuppressWarnings("unchecked")
	STATE reactTo(IdentifiedDomainEvent event, STATE state) {
		return eventHandler.apply((EVENT)event, state);
	}

	public Class<EVENT> getEventClass() {
		return eventClass;
	}
}
