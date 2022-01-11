package org.requirementsascode.being;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.requirementsascode.being.EventHandler.eventsOf;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;

class EventHandlersTest {
	@Test
	void createsEmptyEventHandlers() {
		EventHandlers<State> eventHandlers = EventHandlers.handle();
		
		List<Class<? extends IdentifiedDomainEvent>> eventClasses = eventHandlers.eventClasses();
		assertTrue(eventClasses.isEmpty());
	}

	@Test
	void createsOneEventHandlers() {
		EventHandlers<State> eventHandlers = EventHandlers.handle(
			eventsOf(SampleEvent1.class).with((state,event) -> new State(event.id))
		);
		
		List<Class<? extends IdentifiedDomainEvent>> eventClasses = eventHandlers.eventClasses();
		assertEquals(1, eventClasses.size());
		assertEquals(SampleEvent1.class, eventClasses.get(0));
	}
	
	@Test
	void createsTwoEventHandlers() {
		EventHandlers<State> eventHandlers = EventHandlers.handle(
			eventsOf(SampleEvent1.class).with((state,event) -> new State(event.id)), 
			eventsOf(SampleEvent2.class).with((state,event) -> new State(event.id + "0"))
		);
		List<Class<? extends IdentifiedDomainEvent>> eventClasses = eventHandlers.eventClasses();
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
