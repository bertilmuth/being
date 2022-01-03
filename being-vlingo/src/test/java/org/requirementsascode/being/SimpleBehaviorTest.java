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
    assertEquals(new ArrayList<>(), behaviorTestHelper.events());
    assertEquals(new ArrayList<>(), testAggregateBehavior.state().appliedEvents());
  }

  @Test
  public void emptyGivenEvents() {
    behaviorTestHelper.givenEvents();
    assertEquals(asList(), behaviorTestHelper.events());
    assertEquals(asList(), testAggregateBehavior.state().appliedEvents());
  }
  
  @Test
  public void emptyGivenEventsUnhandledWhen() {
    IgnoredCommand command = new IgnoredCommand();
    behaviorTestHelper
    	.givenEvents()
    	.when(command);
    
    assertEquals(asList(), behaviorTestHelper.events());
    assertEquals(asList(), testAggregateBehavior.state().appliedEvents());
  }
  
  @Test
  public void emptyGivenEventsUnhandledEvent() {
	ProduceUnhandledEventCommand command = new ProduceUnhandledEventCommand();
    behaviorTestHelper
    	.givenEvents()
    	.when(command);
    
    assertEquals(asList(new UnhandledEvent()), behaviorTestHelper.events());
    assertEquals(asList(), testAggregateBehavior.state().appliedEvents());
  }

  @Test
  public void singleGivenEvent() {
    TestEvent givenEvent = new TestEvent("GivenEvent");
    behaviorTestHelper
      .givenEvents(givenEvent);
    
    assertEquals(asList(), behaviorTestHelper.events());
    assertEquals(asList(givenEvent), testAggregateBehavior.state().appliedEvents());
  }
  
  @Test
  public void twoGivenEvents() {
    TestEvent givenEvent1 = new TestEvent("GivenEvent1");
    TestEvent givenEvent2 = new TestEvent("GivenEvent2");

    behaviorTestHelper
      .givenEvents(givenEvent1, givenEvent2);
    
    assertEquals(asList(), behaviorTestHelper.events());
    assertEquals(asList(givenEvent1, givenEvent2), testAggregateBehavior.state().appliedEvents());
  }
  
  @Test
  public void noGivenEventsSingleWhen() {
    TestCommand command = new TestCommand("Command");
    TestEvent resultingEvent = new TestEvent(command.name);
    
    behaviorTestHelper.when(command);
    
    assertEquals(asList(resultingEvent), behaviorTestHelper.events());
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
    
    assertEquals(asList(resultingEvent), behaviorTestHelper.events());
    assertEquals(asList(givenEvent, resultingEvent), testAggregateBehavior.state().appliedEvents());
  }
  
  @Test
  public void singleGivenEventSingleWhen_EventList() {
    TestEvent givenEvent = new TestEvent("GivenEvent");
    TestCommandForEventList command = new TestCommandForEventList("Command");
    TestEvent resultingEvent = new TestEvent(command.name);
    
    behaviorTestHelper
      .givenEvents(givenEvent)
      .when(command);
    
    assertEquals(asList(resultingEvent, resultingEvent), behaviorTestHelper.events());
    assertEquals(asList(givenEvent, resultingEvent, resultingEvent), testAggregateBehavior.state().appliedEvents());
  }
  
  @Test
  public void singleGivenEventTwoWhens_EventList() {
    TestEvent givenEvent = new TestEvent("GivenEvent");
    TestCommandForEventList command = new TestCommandForEventList("Command");
    TestEvent resultingEvent = new TestEvent(command.name);
    
    behaviorTestHelper
      .givenEvents(givenEvent)
      .when(command)
      .when(command);
    
    assertEquals(asList(resultingEvent, resultingEvent, resultingEvent, resultingEvent), behaviorTestHelper.events());
    assertEquals(asList(givenEvent, resultingEvent, resultingEvent, resultingEvent, resultingEvent), testAggregateBehavior.state().appliedEvents());
  }
  
  @Test
  public void eventHandlerUpdatesState() {
    behaviorTestHelper.when(new TestUpdateStateCommand());

    assertEquals(asList(new TestUpdateStateEvent()), behaviorTestHelper.events());
    assertEquals(asList(NEW_AGGREGATE_ROOT_EVENT), testAggregateBehavior.state().appliedEvents());
  }
  
  @Test
  public void eventHandlerUpdatesStateAndHandlesSecondWhen() {
    TestUpdateStateCommand command1 = new TestUpdateStateCommand();
    TestUpdateStateEvent expectedEvent1 = new TestUpdateStateEvent();
    
    TestCommand command2 = new TestCommand("Command2");
    TestEvent expectedEvent2 = new TestEvent(command2.name);

    behaviorTestHelper
      .when(command1)
      .when(command2);

    assertEquals(asList(expectedEvent1, expectedEvent2), behaviorTestHelper.events());
    assertEquals(asList(NEW_AGGREGATE_ROOT_EVENT, expectedEvent2), testAggregateBehavior.state().appliedEvents());
  }

  private static class TestAggregateBehavior extends AggregateBehavior<TestState> {
    @Override
    public CommandHandlers commandHandlers() {
      return CommandHandlers.are(
          commandsOf(TestCommand.class).toEvent(cmd -> new TestEvent(cmd.name)),
          commandsOf(TestUpdateStateCommand.class).toEvent(cmd -> new TestUpdateStateEvent()),
          commandsOf(TestCommandForEventList.class).toEvents(cmd -> {return asList(new TestEvent(cmd.name), new TestEvent(cmd.name));}),
          commandsOf(ProduceUnhandledEventCommand.class).toEvent(cmd -> new UnhandledEvent())
      );
    }

    @Override
    public EventHandlers<TestState> eventHandlers() {
      return EventHandlers.are(
          eventsOf(TestEvent.class).toState(ev -> state().addEvent(ev)),
          eventsOf(TestUpdateStateEvent.class).toState(ev -> new TestState().addEvent(NEW_AGGREGATE_ROOT_EVENT))
      );
    }

    @Override
    public TestState initialState(String aggregateId) {
      return new TestState();
    }
  }

  private static class TestState{
    private final ArrayList<TestEvent> events;

    public TestState() {
      this.events = new ArrayList<>();
    }

    public TestState addEvent(TestEvent testEvent) {
      this.events.add(testEvent);
      return this;
    }
    
    public List<TestEvent> appliedEvents(){
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
