# Being
[![Gitter](https://badges.gitter.im/requirementsascode/community.svg)](https://gitter.im/requirementsascode/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)

The goal of Being is to maximize developer joy and productivity when building event-sourced services with Command Query Responsibility Segregation (CQRS).

In short, [event sourcing](https://martinfowler.com/eaaDev/EventSourcing.html) is about persisting events instead of (just) the current state.
State can be recreated by applying the events in sequence.
Event sourcing can be helpful for auditing purposes, to analyze or rebuild previous system states from a business perspective.
CQRS can be used to build highly scalable applications.

Think of a shopping cart: a typical e-commerce application would only store the state of the cart when the user proceeds to checkout.
What if you want to know which shopping cart items have been *removed* by the user, to optimize the purchasing flow?
That's when storing each event becomes helpful, e.g. *ShoppingCartItemRemoved*.

By using a convention-over-configuration approach, Being reduces the number of concepts to learn. That enables you to focus on building domain logic.  

Being is based on [VLINGO XOOM](https://docs.vlingo.io/). VLINGO XOOM provides a powerful framework for building event-sourced services, based on the Actor Model.
To understand what's going on under Being's hood, or to gain more flexibility, have a look at VLINGO's documentation.

**This project is currently heavily under construction. Please check back soon, when the library will be available.**

## Getting started
The easiest way to get started is by [cloning the samples](https://github.com/bertilmuth/being-samples), and adapting them.

You need to define at least:
* The aggregate for command handling / event-sourcing
* The query model (a.k.a. read model)
* HTTP request handlers
* A few configuration settings

Then you can run your service.

If you don't know what an aggregate is, please read the [following description](https://www.martinfowler.com/bliki/DDD_Aggregate.html).

## The aggregate for command handling / event-sourcing
Here's what happens when an aggregate receives a command:
* Being looks for a handler for the command
* If there's a handler, Being executes it: the handler transforms the commands into one or several events
* Being persists the events
* For each event, Being looks for an event handler
* If there's a handler, Being executes it: the handler transforms the event into the new state of the aggregate
* Being updates the state of the aggregate with the new state

So you need to define the command handlers: which types of commands the aggregate consumes, and which event(s) it produces as a reaction to each command.

You also need to define the event handlers: for each of the event types, which new aggregate state to create as a reaction to it.

As a Hello World style example, the `Greeting` aggregate looks like this:

``` java
public class Greeting implements EventSourcedAggregate<GreetingCommand, GreetingState> {
	@Override
	public GreetingState initialState(final String id) {
		return GreetingState.identifiedBy(id);
	}

	@Override
	public CommandHandlers<GreetingCommand, GreetingState> commandHandlers() {
		return CommandHandlers.handle(
			commandsOf(CreateGreeting.class).with((cmd,state) -> new GreetingCreated(state.id, "Hello, ", cmd.personName)),
			commandsOf(ChangeSalutation.class).with((cmd, state) -> new SalutationChanged(state.id, cmd.salutation))
		);
	}

	@Override
	public EventHandlers<GreetingState> eventHandlers() {
		return EventHandlers.handle(
			eventsOf(GreetingCreated.class).with((event,state) -> new GreetingState(state.id, event.salutation, event.salutation)),
			eventsOf(SalutationChanged.class).with((event,state) -> new GreetingState(state.id, event.salutation, state.personName))
		);
	}
}
```

The first command handler consumes a `CreateGreeting` command that contains the name of the person to greet, and produces a `GreetingCreated` event. 
The fixed salutation *Hello, * is part of that event, so when you later request to greet *Joe*, the greeting will be *Hello, Joe*. 

But a user can also change the salutation via a `ChangeSalutation` command.
This command contains only the new text for the salutation, not the person's name. The person is identified by the aggregate's id, `state.id`. 

Both the command handlers and the event handlers can use the current state of the aggregate.
So when a `SalutationChanged` event is applied, the person name is not taken from the event, but from the current state of the aggregate:
`(event,state) -> new GreetingState(state.id, event.salutation, state.personName)`.
In other words: aggregates are stateful, and keep their state between requests (or recreate the state transparently).

Here's the code for the `GreetingState` class that represents the state of the aggregate:

``` java
public final class GreetingState {
	public final String id;
	public final String salutation;
	public final String personName;
	public final String greetingText;

	public static GreetingState identifiedBy(final String id) {
		return new GreetingState(id, "", "");
	}

	public GreetingState(final String id, final String salutation, final String personName) {
		this.id = id;
		this.salutation = salutation;
		this.personName = personName;
		this.greetingText = salutation + personName;
	}

	@Override
	public String toString() {
		return "GreetingState [id=" + id + ", greetingText=" + greetingText + "]";
	}
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
	QueryModel.startEmpty(() -> GreetingData.empty()) 
		.mergeEventsOf(GreetingCreated.class, (previousData, event) -> GreetingData.from(event.id, event.salutation, event.personName))
		.mergeEventsOf(SalutationChanged.class, (previousData, event) -> GreetingData.from(event.id, event.salutation, previousData.personName));
```
In the same way that you can access the current state of the aggregate in your command/event handlers, you can access the `previousData` in the merge function.

## HTTP request handlers
Use a builder to create the HTTP request handlers:

``` java
HttpRequestHandlers<GreetingCommand, GreetingState, GreetingData> greetingRequestHandlers = 
	HttpRequestHandlers.builder()
		.stage(grid)
		.aggregateSupplier(() -> new Greeting())
		.dataFromState(GreetingData::from)
		.createRequest("/greetings", CreateGreeting.class)
		.updateRequest("/greetings/change/{id}", ChangeSalutation.class)
		.findByIdRequest("/greetings/{id}")
		.findAllRequest("/greetings")
		.build();
```

The stage/grid is used by VLINGO's cluster management.

The aggregate supplier enables Being to create instances of the aggregate.

The `dataFromState` function transforms the state of the aggregate to the data shown to the user:

``` java
public static GreetingData from(final GreetingState greetingState) {
	return from(greetingState.id, greetingState.salutation, greetingState.personName);
}

public static GreetingData from(final String id, final String salutation, final String personName) {
	return new GreetingData(id, salutation, personName);
}
```

The following statements of the builder create the HTTP routes that react to HTTP requests.
Being maps:
*  `createRequest(...)` to a POST request
*  `updateRequest(...)` to a PATCH request
* `findByIdRequest(...)` and `findAllRequest(...)` to a GET request

In the PATCH and POST requests, you use JSON to represent the commands.


You send a GET request to the address defined in the service interface, 
replacing the `:id` part with the id of the aggregate that you want to contact. For example:

Unix: `curl http://localhost:9000/api/greet/Joe`

Windows (PowerShell): `iwr http://localhost:9000/api/greet/Joe`

In this example, the aggregate id is used for the name in the greeting, so the response will be *Hello, Joe!*

Where does the response *Hello, Joe!* come from? It's defined in the `responseToGet()` method
of your aggregate's behavior in the service implementation. [Take a peek](https://github.com/bertilmuth/being-samples/blob/main/greetuser/greetuser-impl/src/main/java/org/requirementsascode/being/greetuser/impl/GreetUserBehavior.java), if you want to.
We'll come back to it.

The class of the response must also be listed in the `responseTypes()`.

### POSTing a command to the aggregate to change aggregate state
To change the aggregate's state, send a POST request to the same address. Its JSON body must contain a `@type` property with the simple class name of a command, e.g. `ChangeGreetingText`. All commands must be listed by the `commandTypes()` method of the service interface. 

Example: To change the greeting text from *Hello, Joe!* to *Hi, Joe!*, send the following POST request:

Unix: `curl -H "Content-Type: application/json" -X POST -d '{"@type": "ChangeGreetingText", "newText":"Hi"}' http://localhost:9000/api/greet/Joe`

Windows (PowerShell): `iwr http://localhost:9000/api/greet/Joe -Method 'POST' -Headers @{'Content-Type' = 'application/json'} -Body '{"@type": "ChangeGreetingText", "newText":"Hi"}'`

Each POST request is processed by the `commandHandlers()` of the [aggregate behavior](https://github.com/bertilmuth/being-samples/blob/main/greetuser/greetuser-impl/src/main/java/org/requirementsascode/being/greetuser/impl/GreetUserBehavior.java).
A command handler transforms the command into an internal event. The event causes the state change.
After the state has changed, further GET requests return the new state, for example the new greeting.


