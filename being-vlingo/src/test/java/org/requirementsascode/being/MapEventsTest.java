package org.requirementsascode.being;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.requirementsascode.being.EventMapper.eventsOf;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;

class MapEventsTest {
	@Test
	void createsEmptyEventMappers() {
		MapEvents<State> mapEvents = MapEvents.with();
		
		List<Class<? extends IdentifiedDomainEvent>> eventClasses = mapEvents.getEventClasses();
		assertTrue(eventClasses.isEmpty());
	}

	@Test
	void createsOneEventMappers() {
		MapEvents<State> mapEvents = MapEvents.with(
			eventsOf(SampleEvent1.class).toState(event -> new State(event.id))
		);
		
		List<Class<? extends IdentifiedDomainEvent>> eventClasses = mapEvents.getEventClasses();
		assertEquals(1, eventClasses.size());
		assertEquals(SampleEvent1.class, eventClasses.get(0));
	}
	
	@Test
	void createsTwoEventMappers() {
		MapEvents<State> mapEvents = MapEvents.with(
			eventsOf(SampleEvent1.class).toState(event -> new State(event.id)), 
			eventsOf(SampleEvent2.class).toState(event -> new State(event.id + "0"))
		);
		List<Class<? extends IdentifiedDomainEvent>> eventClasses = mapEvents.getEventClasses();
		assertEquals(2, eventClasses.size());
		assertEquals(SampleEvent1.class, eventClasses.get(0));
		assertEquals(SampleEvent2.class, eventClasses.get(1));
	}

	private class SampleEvent1 extends IdentifiedDomainEvent{
		public String id;

		@Override
		public String identity() {
			return id;
		}
	};
	
	private class SampleEvent2 extends IdentifiedDomainEvent{
		public String id;

		@Override
		public String identity() {
			return id;
		}
	};

	private class State {
		public final String id;

		State(String id) {
			this.id = id;
		}
		
		@Override
		public String toString() {
			return "State [id=" + id + "]";
		}
	};
}
