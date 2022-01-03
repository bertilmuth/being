package org.requirementsascode.being;

import java.util.Objects;
import java.util.function.Function;

import io.vlingo.xoom.lattice.model.DomainEvent;
import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;
import io.vlingo.xoom.symbio.Source;

public class MapEvent<EVENT extends IdentifiedDomainEvent, STATE> {
	private final Class<EVENT> eventClass;
	private final Function<EVENT, STATE> handler;
	
	public static <EVENT extends IdentifiedDomainEvent> EventsOf<EVENT> eventsOf(Class<EVENT> eventClass) {
		return new EventsOf<EVENT>(eventClass);
	}
	
	public static class EventsOf<EVENT extends IdentifiedDomainEvent>{
		private final Class<EVENT> eventClass;
		
		private EventsOf(Class<EVENT> eventClass){
			this.eventClass = eventClass;
		}
		
		<STATE> MapEvent<EVENT, STATE> toState(Function<EVENT, STATE> handler){
			return new MapEvent<>(eventClass, handler);
		}
	}
	
	private MapEvent(Class<EVENT> eventClass, Function<EVENT, STATE> handler) {
		this.eventClass = Objects.requireNonNull(eventClass, "eventClass must be non-null!");
		this.handler = Objects.requireNonNull(handler, "handler must be non-null!");
	}

	public Class<EVENT> getEventClass() {
		return eventClass;
	}
	
	@SuppressWarnings("unchecked")
	STATE reactTo(Source<DomainEvent> event) {
		return handler.apply((EVENT)event);
	}
}
