package org.requirementsascode.being;

import java.util.Objects;
import java.util.function.Function;

import io.vlingo.xoom.lattice.model.DomainEvent;
import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;
import io.vlingo.xoom.symbio.Source;

public class EventMapper<EVENT extends IdentifiedDomainEvent, STATE> implements Function<Source<DomainEvent>, STATE>{
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
		
		<STATE> EventMapper<EVENT, STATE> toState(Function<EVENT, STATE> mapFunction){
			return new EventMapper<>(eventClass, mapFunction);
		}
	}
	
	private EventMapper(Class<EVENT> eventClass, Function<EVENT, STATE> mapFunction) {
		this.eventClass = Objects.requireNonNull(eventClass, "eventClass must be non-null!");
		this.mapFunction = Objects.requireNonNull(mapFunction, "mapFunction must be non-null!");
	}

	public Class<EVENT> getEventClass() {
		return eventClass;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public STATE apply(Source<DomainEvent> event) {
		return mapFunction.apply((EVENT)event);
	}
}
