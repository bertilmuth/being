package org.requirementsascode.being;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.requirementsascode.being.CommandHandler.commandsOf;
import static org.requirementsascode.being.EventHandler.eventsOf;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;
import io.vlingo.xoom.symbio.Source;

class SimpleBehaviorTest {
	private static final TestEvent1 NEW_AGGREGATE_ROOT_EVENT = new TestEvent1("New aggregate root");

	private TestingAggregateBehavior testingAggregateBehavior;
	private AggregateBehaviorTest<TestCommand, TestState1> aggregateTest;

	@BeforeEach
	public void setup() {
		testingAggregateBehavior = new TestingAggregateBehavior();
		aggregateTest = AggregateBehaviorTest.of(testingAggregateBehavior);
	}

	@Test
	public void noGivenEvents() {
		assertEquals(new ArrayList<>(), aggregateTest.state().appliedEvents());
	}

	@Test
	public void emptyGivenEvents() {
		aggregateTest.givenEvents();
		assertEquals(asList(), aggregateTest.state().appliedEvents());
	}

	@Test
	public void emptyGivenEventsUnhandledWhen() {
		IgnoredCommand command = new IgnoredCommand();
		aggregateTest.givenEvents().when(command);

		assertEquals(asList(), aggregateTest.state().appliedEvents());
	}

	@Test
	public void emptyGivenEventsUnhandledEvent() {
		ProduceUnhandledEventCommand command = new ProduceUnhandledEventCommand();
		aggregateTest.givenEvents().when(command);

		assertEquals(asList(), aggregateTest.state().appliedEvents());
	}

	@Test
	public void singleGivenEvent() {
		TestEvent1 givenEvent = new TestEvent1("GivenEvent");
		aggregateTest.givenEvents(givenEvent);

		assertEquals(asList(givenEvent), aggregateTest.state().appliedEvents());
	}

	@Test
	public void twoGivenEvents() {
		TestEvent1 givenEvent1 = new TestEvent1("GivenEvent1");
		TestEvent1 givenEvent2 = new TestEvent1("GivenEvent2");

		aggregateTest.givenEvents(givenEvent1, givenEvent2);

		assertEquals(asList(givenEvent1, givenEvent2), aggregateTest.state().appliedEvents());
	}

	@Test
	public void noGivenEventsSingleWhen() {
		TestCommand1 command = new TestCommand1("Command");
		TestEvent1 resultingEvent = new TestEvent1(command.name);

		aggregateTest.when(command);

		assertEquals(asList(resultingEvent), aggregateTest.state().appliedEvents());
	}

	@Test
	public void singleGivenEventSingleWhen() {
		TestEvent1 givenEvent = new TestEvent1("GivenEvent");
		TestCommand1 command = new TestCommand1("Command");
		TestEvent1 resultingEvent = new TestEvent1(command.name);

		aggregateTest.givenEvents(givenEvent).when(command);

		assertEquals(asList(givenEvent, resultingEvent), aggregateTest.state().appliedEvents());
	}

	@Test
	public void singleGivenEventSingleWhen_EventList() {
		TestEvent1 givenEvent = new TestEvent1("GivenEvent");
		TestCommandForEventList command = new TestCommandForEventList("Command");

		aggregateTest.givenEvents(givenEvent).when(command);

		TestEvent1 resultingEvent1 = new TestEvent1(command.name);
		TestEvent2 resultingEvent2 = new TestEvent2(command.name);

		assertEquals(asList(givenEvent, resultingEvent1, resultingEvent2), aggregateTest.state().appliedEvents());
	}

	@Test
	public void singleGivenEventTwoWhens_EventList() {
		TestEvent1 givenEvent = new TestEvent1("GivenEvent");
		TestCommandForEventList command = new TestCommandForEventList("Command");

		aggregateTest.givenEvents(givenEvent).when(command).when(command);

		TestEvent1 resultingEvent1 = new TestEvent1(command.name);
		TestEvent2 resultingEvent2 = new TestEvent2(command.name);

		assertEquals(asList(givenEvent, resultingEvent1, resultingEvent2, resultingEvent1, resultingEvent2),
				aggregateTest.state().appliedEvents());
	}

	@Test
	public void appliedEventUpdatesState() {
		aggregateTest.when(new TestUpdateStateCommand());

		assertEquals(asList(NEW_AGGREGATE_ROOT_EVENT), aggregateTest.state().appliedEvents());
	}

	@Test
	public void appliedEventsUpdateState() {
		TestUpdateStateCommand command1 = new TestUpdateStateCommand();

		TestCommand1 command2 = new TestCommand1("Command2");
		TestEvent1 expectedEvent2 = new TestEvent1(command2.name);

		aggregateTest.when(command1).when(command2);

		assertEquals(asList(NEW_AGGREGATE_ROOT_EVENT, expectedEvent2), aggregateTest.state().appliedEvents());
	}

	private static class TestingAggregateBehavior implements AggregateBehavior<TestCommand, TestState1> {
		@Override
		public CommandHandlers<TestCommand, TestState1> commandHandlers() {
			return CommandHandlers.handle(commandsOf(TestCommand1.class).with((cmd, state) -> new TestEvent1(cmd.name)),
					commandsOf(TestUpdateStateCommand.class).with((cmd, state) -> new TestUpdateStateEvent()),
					commandsOf(TestCommandForEventList.class).withSome((cmd, state) -> {
						return asList(new TestEvent1(cmd.name), new TestEvent2(cmd.name));
					}), commandsOf(ProduceUnhandledEventCommand.class).with((cmd, state) -> new UnhandledEvent()));
		}

		@Override
		public EventHandlers<TestState1> eventHandlers() {
			return EventHandlers.handle(eventsOf(TestEvent1.class).with((event, state) -> state.addEvent(event)),
					eventsOf(TestEvent2.class).with((event, state) -> state.addEvent(event)),
					eventsOf(TestUpdateStateEvent.class)
							.with((event, state) -> new TestState1().addEvent(NEW_AGGREGATE_ROOT_EVENT)));
		}

		@Override
		public TestState1 initialState(String aggregateId) {
			return new TestState1();
		}
	}

	private static class TestState1 {
		private final ArrayList<Source<?>> events;

		public TestState1() {
			this.events = new ArrayList<>();
		}

		public TestState1 addEvent(TestEvent1 testEvent) {
			this.events.add(testEvent);
			return this;
		}

		public TestState1 addEvent(TestEvent2 testEvent) {
			this.events.add(testEvent);
			return this;
		}

		public List<Source<?>> appliedEvents() {
			return java.util.Collections.unmodifiableList(events);
		}
	}

	private interface TestCommand {
	}

	private static class TestCommand1 implements TestCommand {
		public final String name;

		public TestCommand1(String name) {
			this.name = name;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TestCommand1 other = (TestCommand1) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}
	}

	private static class TestCommandForEventList implements TestCommand {
		private final String name;

		public TestCommandForEventList(String name) {
			this.name = name;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TestCommandForEventList other = (TestCommandForEventList) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}
	}

	private static class TestUpdateStateCommand implements TestCommand {
	}

	private static class TestEvent1 extends IdentifiedDomainEvent {
		private final String name;

		public TestEvent1(String name) {
			this.name = name;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TestEvent1 other = (TestEvent1) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "TestEvent [name=" + name + "]";
		}

		@Override
		public String identity() {
			return name;
		}
	}

	private static class TestEvent2 extends IdentifiedDomainEvent {
		private final String name;

		public TestEvent2(String name) {
			this.name = name;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TestEvent2 other = (TestEvent2) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "TestEvent2 [name=" + name + "]";
		}

		@Override
		public String identity() {
			return name;
		}
	}

	private static class TestUpdateStateEvent extends IdentifiedDomainEvent {
		@Override
		public boolean equals(Object obj) {
			return obj instanceof TestUpdateStateEvent;
		}

		@Override
		public String identity() {
			return "TestUpdateAggregateRootEvent";
		}
	}

	private static class IgnoredCommand implements TestCommand {
	}

	private static class ProduceUnhandledEventCommand implements TestCommand {
	}

	private static class UnhandledEvent extends IdentifiedDomainEvent {
		@Override
		public String identity() {
			return "1";
		}
	}
}
