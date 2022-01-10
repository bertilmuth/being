package org.requirementsascode.being;

import java.util.Objects;
import java.util.function.Function;

import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;

public class EventHandler<EVENT extends IdentifiedDomainEvent, STATE>{
	private final Class<EVENT> eventClass;
	private final Function<EVENT, STATE> eventHandler;
	
	public static <EVENT extends IdentifiedDomainEvent> EventsOf<EVENT> eventsOf(Class<EVENT> eventClass) {
		return new EventsOf<EVENT>(eventClass);
	}
	
	public static class EventsOf<EVENT extends IdentifiedDomainEvent>{
		private final Class<EVENT> eventClass;
		
		private EventsOf(Class<EVENT> eventClass){
			this.eventClass = eventClass;
		}
		
		public <STATE> EventHandler<EVENT, STATE> with(Function<EVENT, STATE> eventHandler){
			return new EventHandler<>(eventClass, eventHandler);
		}
	}
	
	private EventHandler(Class<EVENT> eventClass, Function<EVENT, STATE> mapFunction) {
		this.eventClass = Objects.requireNonNull(eventClass, "eventClass must be non-null!");
		this.eventHandler = Objects.requireNonNull(mapFunction, "eventHandler must be non-null!");
	}
	
	@SuppressWarnings("unchecked")
	STATE reactTo(IdentifiedDomainEvent event) {
		return eventHandler.apply((EVENT)event);
	}

	public Class<EVENT> getEventClass() {
		return eventClass;
	}
}
