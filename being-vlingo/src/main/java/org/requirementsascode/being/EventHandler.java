package org.requirementsascode.being;

import java.util.Objects;
import java.util.function.Function;

import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;

public class EventHandler<EVENT extends IdentifiedDomainEvent, STATE>{
	private final Class<EVENT> eventClass;
	private final Function<EVENT, STATE> mapFunction;
	
	public static <EVENT extends IdentifiedDomainEvent> EventsOf<EVENT> eventsOf(Class<EVENT> eventClass) {
		return new EventsOf<EVENT>(eventClass);
	}
	
	public static class EventsOf<EVENT extends IdentifiedDomainEvent>{
		private final Class<EVENT> eventClass;
		
		private EventsOf(Class<EVENT> eventClass){
			this.eventClass = eventClass;
		}
		
		public <STATE> EventHandler<EVENT, STATE> toState(Function<EVENT, STATE> mapFunction){
			return new EventHandler<>(eventClass, mapFunction);
		}
	}
	
	private EventHandler(Class<EVENT> eventClass, Function<EVENT, STATE> mapFunction) {
		this.eventClass = Objects.requireNonNull(eventClass, "eventClass must be non-null!");
		this.mapFunction = Objects.requireNonNull(mapFunction, "mapFunction must be non-null!");
	}
	
	@SuppressWarnings("unchecked")
	STATE reactTo(IdentifiedDomainEvent event) {
		return mapFunction.apply((EVENT)event);
	}

	public Class<EVENT> getEventClass() {
		return eventClass;
	}
}
