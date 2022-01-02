package org.requirementsascode.being;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.requirementsascode.being.EventHandler.eventsOf;

import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;
import io.vlingo.xoom.symbio.Source;

class EventHandlersTest {
	@Test
	void createsEmptyEventHandlers() {
		EventHandlers<State> eventHandlers = EventHandlers.are();
		
		List<Class<? extends IdentifiedDomainEvent>> eventClasses = eventHandlers.getEventClasses();
		assertTrue(eventClasses.isEmpty());
		
		List<Function<? extends Source<?>, ?>> handlers = eventHandlers.getHandlers();
		assertTrue(handlers.isEmpty());
	}

	@Test
	void createsOneEventHandler() {
		final Function<SampleEvent1, State> handler = event -> new State(event.id);
		EventHandlers<State> eventHandlers = EventHandlers.are(
			eventsOf(SampleEvent1.class, handler)
		);
		
		List<Class<? extends IdentifiedDomainEvent>> eventClasses = eventHandlers.getEventClasses();
		assertEquals(1, eventClasses.size());
		assertEquals(SampleEvent1.class, eventClasses.get(0));
		
		List<Function<? extends Source<?>, ?>> handlers = eventHandlers.getHandlers();
		assertEquals(1, handlers.size());
		assertEquals(handler, handlers.get(0));
	}
	
	@Test
	void createsTwoEventHandlers() {
		final Function<SampleEvent1, State> handler1 = event -> new State(event.id);
		EventHandler<SampleEvent1, State> eventHandler = eventsOf(SampleEvent1.class, handler1);
		final Function<SampleEvent2, State> handler2 = event -> new State(event.id + "0");
		EventHandler<SampleEvent2, State> eventHandler2 = eventsOf(SampleEvent2.class, handler2);

		EventHandlers<State> eventHandlers = EventHandlers.are(eventHandler, eventHandler2);
		List<Class<? extends IdentifiedDomainEvent>> eventClasses = eventHandlers.getEventClasses();
		assertEquals(2, eventClasses.size());
		assertEquals(SampleEvent1.class, eventClasses.get(0));
		assertEquals(SampleEvent2.class, eventClasses.get(1));
		
		List<Function<? extends Source<?>, ?>> handlers = eventHandlers.getHandlers();
		assertEquals(2, handlers.size());
		assertEquals(handler1, handlers.get(0));
		assertEquals(handler2, handlers.get(1));
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
