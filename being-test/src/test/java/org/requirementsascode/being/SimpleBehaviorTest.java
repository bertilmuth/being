package org.requirementsascode.being;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.requirementsascode.Model;

public class SimpleBehaviorTest {
  private static final TestEvent NEW_AGGREGATE_ROOT_EVENT = new TestEvent("New aggregate root");
  
  private TestAggregateBehavior testAggregateBehavior;
  private AggregateBehaviorTest<TestAggregateRoot> behaviorTest;

  @Before
  public void setup() {
    testAggregateBehavior = new TestAggregateBehavior();
    behaviorTest = AggregateBehaviorTest.of(testAggregateBehavior);
  }

  @Test
  public void noGivenEvents() {
    assertEquals(new ArrayList<>(), behaviorTest.internalEvents());
    assertEquals(new ArrayList<>(), testAggregateBehavior.aggregateRoot().appliedEvents());
  }

  @Test
  public void emptyGivenEvents() {
    behaviorTest.givenEvents();
    assertEquals(new ArrayList<>(), behaviorTest.internalEvents());
    assertEquals(new ArrayList<>(), testAggregateBehavior.aggregateRoot().appliedEvents());
  }
  
  @Test
  public void unknownGivenEventsIsIgnored() {
    behaviorTest.givenEvents("I should be ignored");
    assertEquals(new ArrayList<>(), behaviorTest.internalEvents());
    assertEquals(new ArrayList<>(), testAggregateBehavior.aggregateRoot().appliedEvents());
  }
  
  @Test
  public void commandHandlerThatDoesntPublishEventsIsIgnored() {
    behaviorTest.givenEvents(new NoOpTestCommand());
    assertEquals(new ArrayList<>(), behaviorTest.internalEvents());
    assertEquals(new ArrayList<>(), testAggregateBehavior.aggregateRoot().appliedEvents());
  }

  @Test
  public void singleGivenEvent() {
    TestEvent givenEvent = new TestEvent("GivenEvent");
    behaviorTest
      .givenEvents(givenEvent);
    
    assertEquals(asList(), behaviorTest.internalEvents());
    assertEquals(asList(givenEvent), testAggregateBehavior.aggregateRoot().appliedEvents());
  }
  
  @Test
  public void twoGivenEvents() {
    TestEvent givenEvent1 = new TestEvent("GivenEvent1");
    TestEvent givenEvent2 = new TestEvent("GivenEvent2");

    behaviorTest
      .givenEvents(givenEvent1, givenEvent2);
    
    assertEquals(asList(), behaviorTest.internalEvents());
    assertEquals(asList(givenEvent1, givenEvent2), testAggregateBehavior.aggregateRoot().appliedEvents());
  }
  
  @Test
  public void noGivenEventsSingleWhen() {
    TestCommand command = new TestCommand("Command");
    TestEvent resultingEvent = new TestEvent(command.name);
    
    behaviorTest.when(command);
    
    assertEquals(asList(resultingEvent), behaviorTest.internalEvents());
    assertEquals(asList(resultingEvent), testAggregateBehavior.aggregateRoot().appliedEvents());
  }
  
  @Test
  public void singleGivenEventSingleWhen() {
    TestEvent givenEvent = new TestEvent("GivenEvent");
    TestCommand command = new TestCommand("Command");
    TestEvent resultingEvent = new TestEvent(command.name);
    
    behaviorTest
      .givenEvents(givenEvent)
      .when(command);
    
    assertEquals(asList(resultingEvent), behaviorTest.internalEvents());
    assertEquals(asList(givenEvent, resultingEvent), testAggregateBehavior.aggregateRoot().appliedEvents());
  }
  
  @Test
  public void singleGivenEventSingleWhen_EventList() {
    TestEvent givenEvent = new TestEvent("GivenEvent");
    TestCommandForEventList command = new TestCommandForEventList("Command");
    TestEvent resultingEvent = new TestEvent(command.name);
    
    behaviorTest
      .givenEvents(givenEvent)
      .when(command);
    
    assertEquals(asList(resultingEvent, resultingEvent), behaviorTest.internalEvents());
    assertEquals(asList(givenEvent, resultingEvent, resultingEvent), testAggregateBehavior.aggregateRoot().appliedEvents());
  }
  
  @Test
  public void singleGivenEventTwoWhens_EventList() {
    TestEvent givenEvent = new TestEvent("GivenEvent");
    TestCommandForEventList command = new TestCommandForEventList("Command");
    TestEvent resultingEvent = new TestEvent(command.name);
    
    behaviorTest
      .givenEvents(givenEvent)
      .when(command)
      .when(command);
    
    assertEquals(asList(resultingEvent, resultingEvent, resultingEvent, resultingEvent), behaviorTest.internalEvents());
    assertEquals(asList(givenEvent, resultingEvent, resultingEvent, resultingEvent, resultingEvent), testAggregateBehavior.aggregateRoot().appliedEvents());
  }
  
  @Test
  public void singleGivenEventSingleWhen_EventSet() {
    final String name1 = "Command1";
    final String name2 = "Command2";

    TestEvent givenEvent = new TestEvent("GivenEvent");
    TestCommandForEventSet command = new TestCommandForEventSet(name1, name2);
    TestEvent event1 = new TestEvent(name1);
    TestEvent event2 = new TestEvent(name2);
    
    behaviorTest
      .givenEvents(givenEvent)
      .when(command);
    
    assertEquals(asList(event1, event2), behaviorTest.internalEvents());
    assertEquals(asList(givenEvent, event1, event2), testAggregateBehavior.aggregateRoot().appliedEvents());
  }
  
  @Test
  public void internalEventHandlerUpdatesAggregateRoot() {
    behaviorTest.when(new TestUpdateAggregateRootCommand());

    assertEquals(asList(new TestUpdateAggregateRootEvent()), behaviorTest.internalEvents());
    assertEquals(asList(NEW_AGGREGATE_ROOT_EVENT), testAggregateBehavior.aggregateRoot().appliedEvents());
  }
  
  @Test
  public void internalEventHandlerUpdatesAggregateRootAndHandlesSecondWhen() {
    TestUpdateAggregateRootCommand command1 = new TestUpdateAggregateRootCommand();
    TestUpdateAggregateRootEvent expectedEvent1 = new TestUpdateAggregateRootEvent();
    
    TestCommand command2 = new TestCommand("Command2");
    TestEvent expectedEvent2 = new TestEvent(command2.name);

    behaviorTest
      .when(command1)
      .when(command2);

    assertEquals(asList(expectedEvent1, expectedEvent2), behaviorTest.internalEvents());
    assertEquals(asList(NEW_AGGREGATE_ROOT_EVENT, expectedEvent2), testAggregateBehavior.aggregateRoot().appliedEvents());
  }
  
  @Test(expected = AggregateBehavior.IllegalSystemPublish.class)
  public void internalEventHandlerFailsToUpdateAggregateRoot_wrongTypeIsPublished() {
    behaviorTest.when(new TestFailingUpdateAggregateRootCommand());
  }

  private static class TestAggregateBehavior extends AggregateBehavior<TestAggregateRoot> {
    @Override
    public Model commandHandlers() {
      return Model.builder()
          .user(TestCommand.class).systemPublish(cmd -> new TestEvent(cmd.name))
          .user(TestCommandForEventList.class).systemPublish(cmd -> {return asList(new TestEvent(cmd.name), new TestEvent(cmd.name));})
          .user(TestCommandForEventSet.class).systemPublish(cmd -> {return new LinkedHashSet<>(asList(new TestEvent(cmd.name1), new TestEvent(cmd.name2)));})
          .user(NoOpTestCommand.class).system(cmd -> {})
          .user(TestUpdateAggregateRootCommand.class).systemPublish(cmd -> new TestUpdateAggregateRootEvent())
          .user(TestFailingUpdateAggregateRootCommand.class).systemPublish(cmd -> new TestFailingUpdateAggregateRootEvent())
          // The following event should not be handled, because it should not be published to this model in the first place
          .user(TestEvent.class).system(ev -> {throw new RuntimeException("Illegal test event in requestMessageHandlers");})
          .build();
    }

    @Override
    public Model internalEventHandlers() {
      return Model.builder()
          .on(TestEvent.class).system(ev -> aggregateRoot().addEvent(ev))
          .on(TestUpdateAggregateRootEvent.class).systemPublish(ev -> new TestAggregateRoot().addEvent(NEW_AGGREGATE_ROOT_EVENT))
          .on(TestFailingUpdateAggregateRootEvent.class).systemPublish(ev -> "This should be an aggregate root, so it will fail!")
          // The following line should not be handled, because it should not be published to this model in the first place
          .user(TestAggregateRoot.class).system(ev -> {throw new RuntimeException("Illegal aggregate root in internalEventHandlers");})
          .build();
    }

    @Override
    public TestAggregateRoot createAggregateRoot(String aggregateId) {
      return new TestAggregateRoot();
    }

    @Override
    public Object responseToGet() {
      return aggregateRoot();
    }
  }

  private static class TestAggregateRoot{
    private final ArrayList<TestEvent> events;

    public TestAggregateRoot() {
      this.events = new ArrayList<>();
    }

    public TestAggregateRoot addEvent(TestEvent testEvent) {
      this.events.add(testEvent);
      return this;
    }
    
    public List<TestEvent> appliedEvents(){
      return java.util.Collections.unmodifiableList(events);
    }
  } 

  private static class TestCommand{
    private final String name;

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
  
  private static class TestCommandForEventSet{
    private final String name1;
    private final String name2;

    public TestCommandForEventSet(String name1, String name2) {
      this.name1 = name1;
      this.name2 = name2;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((name1 == null) ? 0 : name1.hashCode());
      result = prime * result + ((name2 == null) ? 0 : name2.hashCode());
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
      TestCommandForEventSet other = (TestCommandForEventSet) obj;
      if (name1 == null) {
        if (other.name1 != null)
          return false;
      } else if (!name1.equals(other.name1))
        return false;
      if (name2 == null) {
        if (other.name2 != null)
          return false;
      } else if (!name2.equals(other.name2))
        return false;
      return true;
    }
    
    

  }
  
  private static class NoOpTestCommand{
  }
  
  private static class TestUpdateAggregateRootCommand{
  }
  
  private static class TestFailingUpdateAggregateRootCommand{
  }

  private static class TestEvent{
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
  }
  
  private static class TestUpdateAggregateRootEvent{
    @Override
    public boolean equals(Object obj) {
      return obj instanceof TestUpdateAggregateRootEvent;
    }
  }
  
  private static class TestFailingUpdateAggregateRootEvent{
    @Override
    public boolean equals(Object obj) {
      return obj instanceof TestFailingUpdateAggregateRootEvent;
    }
  }
}
