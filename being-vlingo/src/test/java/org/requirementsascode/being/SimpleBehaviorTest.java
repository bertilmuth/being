package org.requirementsascode.being;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.requirementsascode.being.MapCommand.commandsOf;
import static org.requirementsascode.being.MapEvent.eventsOf;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;
import io.vlingo.xoom.symbio.Source;

class SimpleBehaviorTest {
  private static final TestEvent NEW_AGGREGATE_ROOT_EVENT = new TestEvent("New aggregate root");
  
  private TestAggregateBehavior testAggregateBehavior;
  private BehaviorTestHelper<TestState> behaviorTestHelper;

  @BeforeEach
  public void setup() {
    testAggregateBehavior = new TestAggregateBehavior();
    behaviorTestHelper = BehaviorTestHelper.of(testAggregateBehavior);
  }

  @Test
  public void noGivenEvents() {
    assertEquals(new ArrayList<>(), behaviorTestHelper.producedEvents());
    assertEquals(new ArrayList<>(), testAggregateBehavior.state().appliedEvents());
  }

  @Test
  public void emptyGivenEvents() {
    behaviorTestHelper.givenEvents();
    assertEquals(asList(), behaviorTestHelper.producedEvents());
    assertEquals(asList(), testAggregateBehavior.state().appliedEvents());
  }
  
  @Test
  public void emptyGivenEventsUnhandledWhen() {
    IgnoredCommand command = new IgnoredCommand();
    behaviorTestHelper
    	.givenEvents()
    	.when(command);
    
    assertEquals(asList(), behaviorTestHelper.producedEvents());
    assertEquals(asList(), testAggregateBehavior.state().appliedEvents());
  }
  
  @Test
  public void emptyGivenEventsUnhandledEvent() {
	ProduceUnhandledEventCommand command = new ProduceUnhandledEventCommand();
    behaviorTestHelper
    	.givenEvents()
    	.when(command);
    
    assertEquals(asList(new UnhandledEvent()), behaviorTestHelper.producedEvents());
    assertEquals(asList(), testAggregateBehavior.state().appliedEvents());
  }

  @Test
  public void singleGivenEvent() {
    TestEvent givenEvent = new TestEvent("GivenEvent");
    behaviorTestHelper
      .givenEvents(givenEvent);
    
    assertEquals(asList(), behaviorTestHelper.producedEvents());
    assertEquals(asList(givenEvent), testAggregateBehavior.state().appliedEvents());
  }
  
  @Test
  public void twoGivenEvents() {
    TestEvent givenEvent1 = new TestEvent("GivenEvent1");
    TestEvent givenEvent2 = new TestEvent("GivenEvent2");

    behaviorTestHelper
      .givenEvents(givenEvent1, givenEvent2);
    
    assertEquals(asList(), behaviorTestHelper.producedEvents());
    assertEquals(asList(givenEvent1, givenEvent2), testAggregateBehavior.state().appliedEvents());
  }
  
  @Test
  public void noGivenEventsSingleWhen() {
    TestCommand command = new TestCommand("Command");
    TestEvent resultingEvent = new TestEvent(command.name);
    
    behaviorTestHelper.when(command);
    
    assertEquals(asList(resultingEvent), behaviorTestHelper.producedEvents());
    assertEquals(asList(resultingEvent), testAggregateBehavior.state().appliedEvents());
  }
  
  @Test
  public void singleGivenEventSingleWhen() {
    TestEvent givenEvent = new TestEvent("GivenEvent");
    TestCommand command = new TestCommand("Command");
    TestEvent resultingEvent = new TestEvent(command.name);
    
    behaviorTestHelper
      .givenEvents(givenEvent)
      .when(command);
    
    assertEquals(asList(resultingEvent), behaviorTestHelper.producedEvents());
    assertEquals(asList(givenEvent, resultingEvent), testAggregateBehavior.state().appliedEvents());
  }
  
  @Test
  public void singleGivenEventSingleWhen_EventList() {
    TestEvent givenEvent = new TestEvent("GivenEvent");
    TestCommandForEventList command = new TestCommandForEventList("Command");
    
    behaviorTestHelper
      .givenEvents(givenEvent)
      .when(command);
    
    TestEvent resultingEvent1 = new TestEvent(command.name);
    TestEvent2 resultingEvent2 = new TestEvent2(command.name);
    
    assertEquals(asList(resultingEvent1, resultingEvent2), behaviorTestHelper.producedEvents());
    assertEquals(asList(givenEvent, resultingEvent1, resultingEvent2), testAggregateBehavior.state().appliedEvents());
  }
  
  @Test
  public void singleGivenEventTwoWhens_EventList() {
    TestEvent givenEvent = new TestEvent("GivenEvent");
    TestCommandForEventList command = new TestCommandForEventList("Command");
    
    behaviorTestHelper
      .givenEvents(givenEvent)
      .when(command)
      .when(command);
    
    TestEvent resultingEvent1 = new TestEvent(command.name);
    TestEvent2 resultingEvent2 = new TestEvent2(command.name);

    assertEquals(asList(resultingEvent1, resultingEvent2, resultingEvent1, resultingEvent2), behaviorTestHelper.producedEvents());
    assertEquals(asList(givenEvent, resultingEvent1, resultingEvent2, resultingEvent1, resultingEvent2), testAggregateBehavior.state().appliedEvents());
  }
  
  @Test
  public void appliedEventUpdatesState() {
    behaviorTestHelper.when(new TestUpdateStateCommand());

    assertEquals(asList(new TestUpdateStateEvent()), behaviorTestHelper.producedEvents());
    assertEquals(asList(NEW_AGGREGATE_ROOT_EVENT), testAggregateBehavior.state().appliedEvents());
  }
  
  @Test
  public void appliedEventsUpdateState() {
    TestUpdateStateCommand command1 = new TestUpdateStateCommand();
    TestUpdateStateEvent expectedEvent1 = new TestUpdateStateEvent();
    
    TestCommand command2 = new TestCommand("Command2");
    TestEvent expectedEvent2 = new TestEvent(command2.name);

    behaviorTestHelper
      .when(command1)
      .when(command2);

    assertEquals(asList(expectedEvent1, expectedEvent2), behaviorTestHelper.producedEvents());
    assertEquals(asList(NEW_AGGREGATE_ROOT_EVENT, expectedEvent2), testAggregateBehavior.state().appliedEvents());
  }

  private static class TestAggregateBehavior extends AggregateBehavior<TestState> {
    @Override
    public MapCommands mapCommands() {
      return MapCommands.with(
          commandsOf(TestCommand.class).toEvent(cmd -> new TestEvent(cmd.name)),
          commandsOf(TestUpdateStateCommand.class).toEvent(cmd -> new TestUpdateStateEvent()),
          commandsOf(TestCommandForEventList.class).toEvents(cmd -> {return asList(new TestEvent(cmd.name), new TestEvent2(cmd.name));}),
          commandsOf(ProduceUnhandledEventCommand.class).toEvent(cmd -> new UnhandledEvent())
      );
    }

    @Override
    public MapEvents<TestState> mapEvents() {
      return MapEvents.with(
          eventsOf(TestEvent.class).toState(ev -> state().addEvent(ev)),
          eventsOf(TestEvent2.class).toState(ev -> state().addEvent(ev)),
          eventsOf(TestUpdateStateEvent.class).toState(ev -> new TestState().addEvent(NEW_AGGREGATE_ROOT_EVENT))
      );
    }

    @Override
    public TestState initialState(String aggregateId) {
      return new TestState();
    }
  }

  private static class TestState{
    private final ArrayList<Source<?>> events;

    public TestState() {
      this.events = new ArrayList<>();
    }

    public TestState addEvent(TestEvent testEvent) {
      this.events.add(testEvent);
      return this;
    }
    
    public TestState addEvent(TestEvent2 testEvent) {
        this.events.add(testEvent);
        return this;
      }
    
    public List<Source<?>> appliedEvents(){
      return java.util.Collections.unmodifiableList(events);
    }
  } 

  private static class TestCommand{
    public final String name;

    public TestCommand(String name) {
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
      TestCommand other = (TestCommand) obj;
      if (name == null) {
        if (other.name != null)
          return false;
      } else if (!name.equals(other.name))
        return false;
      return true;
    }
  }
  
  private static class TestCommandForEventList{
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
  
  private static class TestUpdateStateCommand{
  }

  private static class TestEvent extends IdentifiedDomainEvent{
    private final String name;

    public TestEvent(String name) {
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
      TestEvent other = (TestEvent) obj;
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
  
  private static class TestEvent2 extends IdentifiedDomainEvent{
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
  
  private static class TestUpdateStateEvent extends IdentifiedDomainEvent{
    @Override
    public boolean equals(Object obj) {
      return obj instanceof TestUpdateStateEvent;
    }

	@Override
	public String identity() {
		return "TestUpdateAggregateRootEvent";
	}
  }
  
  private static class IgnoredCommand{
  }
  
  private static class ProduceUnhandledEventCommand{
  }
  
  private static class UnhandledEvent extends IdentifiedDomainEvent{
	@Override
	public String identity() {
		return "1";
	}
  }
}
