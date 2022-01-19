# Being
[![Gitter](https://badges.gitter.im/requirementsascode/community.svg)](https://gitter.im/requirementsascode/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)

The goal of Being is to shorten the development time when building event-sourced applications with Command Query Responsibility Segregation (CQRS).

In short, [event sourcing](https://martinfowler.com/eaaDev/EventSourcing.html) is about persisting events instead of the current state.
State can be recreated by applying the events in sequence.
Event sourcing can be helpful for auditing purposes, to analyze or rebuild previous system states from a business perspective.
CQRS can be used to build highly scalable applications.

Think of a shopping cart: a typical e-commerce application would only store the state of the cart when the user proceeds to checkout.
What if you want to know which shopping cart items have been *removed* by the user, to optimize the purchasing flow?
That's when storing each event becomes helpful, e.g. *ShoppingCartItemRemoved*.

By using a convention-over-configuration approach, Being reduces the number of concepts to learn. That enables you to focus on building domain logic.  

Being is based on [VLINGO XOOM](https://docs.vlingo.io/). VLINGO XOOM provides a powerful framework for building event-sourced services, based on the Actor Model.
To understand what's going on under Being's hood, or to gain more flexibility, have a look at VLINGO's documentation.

## Getting started
If you are using Maven, include the following in your POM, to use Being and VLINGO XOOM:

``` XML
<dependency>
  <groupId>org.requirementsascode.being</groupId>
  <artifactId>being-core</artifactId>
  <version>0.1.4</version>
  <scope>compile</scope>
</dependency>
<dependency>
  <groupId>io.vlingo.xoom</groupId>
  <artifactId>xoom-turbo</artifactId>
  <version>1.9.0</version>
  <scope>compile</scope>
</dependency>
```

If you are using Gradle, include the following in your build.gradle, to use Being and VLINGO XOOM:

``` Groovy
dependencies {
  implementation 'io.vlingo.xoom:xoom-turbo:1.9.0'
  implementation 'org.requirementsascode.being:being-core:0.1.4'
}
```

If you want to use the testing support of Being, you need it as a dependency as well:

Maven:

``` XML
<dependency>
  <groupId>org.requirementsascode.being</groupId>
  <artifactId>being-test</artifactId>
  <version>0.1.4</version>
  <scope>test</scope>
</dependency>
```

Gradle:

`testImplementation 'org.requirementsascode.being:being-test:0.1.4'`


But since you have to make some configuration settings as well, the easiest way to get started is by [cloning the samples](https://github.com/bertilmuth/being-samples), and adapting them.

## What you need to do to implement event sourcing

You need to define at least:
* The aggregate's command handling behavior
* The aggregate's state
* The query model (a.k.a. read model)
* HTTP request handlers
* A few configuration settings

Then you can run and test your service.

If you don't know what an aggregate is, read [Martin Fowler's description](https://www.martinfowler.com/bliki/DDD_Aggregate.html), as an example.

## The aggregate's command handling behavior
Here's what happens when an aggregate receives a command:
* Being looks for a handler for the command
* If there's a handler, Being executes it: the handler transforms the commands into one or several events
* Being persists the events
* For each event, Being looks for an event handler
* If there's a handler, Being executes it: the handler transforms the event into the new state of the aggregate
* Being updates the state of the aggregate with the new state

So you need to define the command handlers: which types of commands the aggregate consumes, and which event(s) it produces as a reaction to each command.

You also need to define the event handlers: for each of the event types, which new aggregate state to create as a reaction to it.

As a Hello World style example, the `Greeting` aggregate behavior looks like this:

``` java
public class Greeting implements AggregateBehavior<GreetingCommand, GreetingState> {
	@Override
	public GreetingState initialState(final String id) {
		return GreetingState.identifiedBy(id);
	}

	@Override
	public CommandHandlers<GreetingCommand, GreetingState> commandHandlers() {
		return CommandHandlers.handle(
			commandsOf(CreateGreeting.class).with((cmd,state) -> new GreetingCreated(state.id, "Hello,", cmd.personName)),
			commandsOf(ChangeSalutation.class).with((cmd, state) -> new SalutationChanged(state.id, cmd.salutation))
		);
	}

	@Override
	public EventHandlers<GreetingState> eventHandlers() {
		return EventHandlers.handle(
			eventsOf(GreetingCreated.class).with((event,state) -> new GreetingState(event.id, event.salutation, event.personName)),
			eventsOf(SalutationChanged.class).with((event,state) -> new GreetingState(event.id, event.salutation, state.personName))
		);
	}
}
```

The first command handler consumes a `CreateGreeting` command that contains the name of the person to greet, and produces a `GreetingCreated` event. 
The fixed salutation *Hello,* is part of that event, so when you request a greeting for *Joe*, the greeting text is *Hello, Joe*. 

But a user can also change the salutation via a `ChangeSalutation` command.
This command contains only the new text for the salutation, not the person's name. The person is identified by the aggregate's id, `state.id`. 

Both the command handlers and the event handlers can use the current state of the aggregate.
So when a `SalutationChanged` event is applied, the person name is not taken from the event, but from the current state of the aggregate:
`(event,state) -> new GreetingState(event.id, event.salutation, state.personName)`.
In other words: aggregates are stateful, and keep their state between requests (or recreate the state transparently).

## The aggregate's state
Here's the code for the `GreetingState` class that represents the state of the aggregate:

``` java
public final class GreetingState {
	public final String id;
	public final String salutation;
	public final String personName;

	public static GreetingState identifiedBy(final String id) {
		return new GreetingState(id, "", "");
	}

	public GreetingState(final String id, final String salutation, final String personName) {
		this.id = id;
		this.salutation = salutation;
		this.personName = personName;
	}

	@Override
	public String toString() {
		return "GreetingState [id=" + id + ", salutation=" + salutation + ", personName=" + personName + "]";
	}
	
	// hashCode() and equals() omitted for brevity
}
```
As you can see, objects of the state class are immutable. 

### Commands
Commands are simple POJOs, as you can see in the following example:

``` java
public class CreateGreeting implements GreetingCommand{
	public final String personName;

	public CreateGreeting(String personName) {
		this.personName = personName;
	}
	
	@Override
	public String toString() {
		return "CreateGreeting [personName=" + personName + "]";
	}
}
```
Commands of an aggregate implement a common interface, like `GreetingCommand` in the example, which may be empty:

``` java
public interface GreetingCommand {
}
```
The reason for having a common interface for the commands is type safety. Use this command interface as the first type parameter of the aggregate class, as shown above:

``` java
public class Greeting implements EventSourcedAggregate<GreetingCommand, GreetingState> {
	...
}
```

### Events
Each event class must be a subclass of `IdentifiedDomainEvent`:

``` java
import io.vlingo.xoom.common.version.SemanticVersion;
import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;

public final class GreetingCreated extends IdentifiedDomainEvent {
	public final String id;
	public final String salutation;
	public final String personName;

	public GreetingCreated(final String id, final String salutation, String personName) {
	    super(SemanticVersion.from("1.0.0").toValue());
		this.id = id;
		this.salutation = salutation;
		this.personName = personName;
	}

	@Override
	public String identity() {
		return id;
	}

	@Override
	public String toString() {
		return "GreetingCreated [id=" + id + ", salutation=" + salutation + ", personName=" + personName + "]";
	}
}
```
The event class constructor passes the event version to the super class.
The event identity matches the identity of the aggregate's state.

## The query model (a.k.a. read model)
The query model defines how the data that the user sees is created from the events.
You as the programmer need to define the empty data as a starting point, and how to merge
each event type that you want to show into the data:

``` java
QueryModel<GreetingData> queryModel = 
	QueryModel.startEmpty(GreetingData.empty()) 
		.mergeEventsOf(GreetingCreated.class, 
			(event,previousData) -> GreetingData.from(event.id, event.salutation, event.personName))
		.mergeEventsOf(SalutationChanged.class, 
			(event,previousData) -> GreetingData.from(event.id, event.salutation, previousData.personName));
```
In the same way that you can access the current state of the aggregate in your command/event handlers, you can access the `previousData` in the merge function.

## HTTP request handlers
Use a builder to create the HTTP request handlers:

``` java
HttpRequestHandlers<GreetingCommand, GreetingState, GreetingData> greetingRequestHandlers = 
	HttpRequestHandlers.builder()
		.stage(grid)
		.behaviorSupplier(() -> new Greeting())
		.queryDataFromState(GreetingData::from)
		.createRequest(CREATE_PATH, CreateGreeting.class)
		.updateRequest(UPDATE_PATH, ChangeSalutation.class)
		.findByIdRequest(FIND_BY_ID_PATH)
		.findAllRequest(FIND_ALL_PATH)
		.build();
```

The stage/grid is used by VLINGO's cluster management.

The aggregate supplier enables Being to create instances of the aggregate.

The `queryDataFromState` function transforms the state of the aggregate to the data shown to the user:

``` java
public static GreetingData from(final GreetingState state) {
	return from(state.id, state.salutation, state.personName);
}

public static GreetingData from(final String id, final String salutation, final String personName) {
	return new GreetingData(id, personName, salutation + " " + personName);
}
```

The following statements of the builder create the HTTP routes that react to HTTP requests.
Being maps:
*  `createRequest(...)` to a POST request
*  `updateRequest(...)` to a PATCH request
* `findByIdRequest(...)` and `findAllRequest(...)` to a GET request

## Configuration
Further things you need to do:
* Adapt the [Bootstrap](https://github.com/bertilmuth/being-samples/blob/main/greetings/src/main/java/org/requirementsascode/being/samples/greeting/infrastructure/Bootstrap.java) class that wires everything together

* If necessary, adapt the resource files [xoom-actors.properties](https://github.com/bertilmuth/being-samples/blob/main/greetings/src/main/resources/xoom-actors.properties), [xoom-cluster.properties](https://github.com/bertilmuth/being-samples/blob/main/greetings/src/main/resources/xoom-cluster.properties) and [xoom-turbo.properties](https://github.com/bertilmuth/being-samples/blob/main/greetings/src/main/resources/xoom-turbo.properties). 

Have a look at the [VLINGO documentation](https://docs.vlingo.io/) for details.

## Testing the aggregate
For fast running unit tests, here's an example of what a test can look like:

``` java
void updatesGreetingOnce() {
	behaviorTest
		.givenEvents(new GreetingCreated("#1", "Hi", "Jill"))
		.when(new ChangeSalutation("Hello"));
	
	final GreetingState expectedState = new GreetingState("#1", "Hello", "Jill");
	assertThat(behaviorTest.state(), is(expectedState));
}
```
These kinds of tests run outside of any infrastructure. 
They simply check if applying the command handlers and event handlers in sequence yield the expected result.
Have a look at the [source code](https://github.com/bertilmuth/being-samples/blob/main/greetings/src/test/java/org/requirementsascode/being/samples/greeting/model/GreetingTest.java) for more details.

Of course, you can also run integration tests with a library such as [REST Assured](https://github.com/rest-assured/rest-assured) that
directly work with the HTTP endpoints:

``` java
Response greetingData = 
	givenJsonClient()
		.body("{\"personName\":\"" + personName + "\"}")
		.post(CREATE_PATH);

final String expectedGreetingText = salutation + " " + personName;

assertThat(json(greetingData, "personName"), is(personName));
assertThat(json(greetingData, "greetingText"), is(expectedGreetingText));
```

Have a look at the [source code](https://github.com/bertilmuth/being-samples/blob/main/greetings/src/test/java/org/requirementsascode/being/samples/greeting/infrastructure/HttpTest.java) for more details.

## Starting the server
Follow these steps:
1. Open a shell and change to the root directory of the application
2. Run the server with `./gradlew run`
3. Open a second shell. Now you can start sending requests.

## Sending requests to the aggregate
In POST requests (for creating) and PATCH requests (for updating), use JSON to represent the commands.

Here are some example commands, together with example reponses from the server.

NOTE: On Windows 10 systems, use `curl.exe` instead of `curl`.

Create a greeting for Joe:

`curl -i -X POST -H "Content-Type: application/json" -d '{"personName":"Joe"}' http://localhost:8081/greetings`

Example response: 

``` shell
HTTP/1.1 201 Created
Content-Type: application/json; charset=UTF-8
Content-Length: 92

{"id":"898954e3-a886-4352-9283-320fc3a66c09","personName":"Joe","greetingText":"Hello, Joe"}
```

Afterwards, get Joe's greeting: 

`curl http://localhost:8081/greetings/898954e3-a886-4352-9283-320fc3a66c09`

Example response: 

`{"id":"898954e3-a886-4352-9283-320fc3a66c09","personName":"Joe","greetingText":"Hello, Joe"}`

Change Joe's greeting:

`curl -i -X PATCH -H "Content-Type: application/json" -d '{"salutation":"Howdy"}' http://localhost:8081/greetings/change/898954e3-a886-4352-9283-320fc3a66c09`

Example response: 

``` shell
HTTP/1.1 200 OK
Content-Type: application/json; charset=UTF-8
Content-Length: 91

{"id":"898954e3-a886-4352-9283-320fc3a66c09","personName":"Joe","greetingText":"Howdy Joe"}
```

Create a greeting for Jill: 

`curl -i -X POST -H "Content-Type: application/json" -d '{"personName":"Jill"}' http://localhost:8081/greetings`

Get all greetings: 

`curl.exe http://localhost:8081/greetings`

Example response: 

`[{"id":"898954e3-a886-4352-9283-320fc3a66c09","personName":"Joe","greetingText":"Howdy Joe"},{"id":"c37bfde8-4247-4c63-8607-d0453182859f","personName":"Jill","greetingText":"Hello, Jill"}]`