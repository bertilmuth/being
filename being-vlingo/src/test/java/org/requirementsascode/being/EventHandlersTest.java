package org.requirementsascode.being;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.requirementsascode.being.EventHandler.on;

import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;
import io.vlingo.xoom.symbio.Source;

class EventHandlersTest {
	@Test
	void createsEmptyEventHandlers() {
		EventHandlers eventHandlers = EventHandlers.are();
		
		List<Class<? extends Source<?>>> events = eventHandlers.getEventClasses();
		assertTrue(events.isEmpty());
		
		List<Function<? extends Source<?>, ?>> handlers = eventHandlers.getHandlers();
		assertTrue(handlers.isEmpty());
	}

	@Test
	void createsOneEventHandler() {
		final Function<SampleEvent1, ?> handler = event -> new State(event.id);
		EventHandlers eventHandlers = EventHandlers.are(
			on(SampleEvent1.class, handler)
		);
		
		List<Class<? extends Source<?>>> events = eventHandlers.getEventClasses();
		assertEquals(1, events.size());
		assertEquals(SampleEvent1.class, events.get(0));
		
		List<Function<? extends Source<?>, ?>> handlers = eventHandlers.getHandlers();
		assertEquals(1, handlers.size());
		assertEquals(handler, handlers.get(0));
	}
	
	@Test
	void createsTwoEventHandlers() {
		final Function<SampleEvent1, ?> handler1 = event -> new State(event.id);
		EventHandler<SampleEvent1> eventHandler = on(SampleEvent1.class, handler1);
		final Function<SampleEvent2, ?> handler2 = event -> new State(event.id + "0");
		EventHandler<SampleEvent2> eventHandler2 = on(SampleEvent2.class, handler2);

		EventHandlers eventHandlers = EventHandlers.are(eventHandler, eventHandler2);
		List<Class<? extends Source<?>>> events = eventHandlers.getEventClasses();
		assertEquals(2, events.size());
		assertEquals(SampleEvent1.class, events.get(0));
		assertEquals(SampleEvent2.class, events.get(1));
		
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
