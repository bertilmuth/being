# Being
[![Build Status](https://travis-ci.com/bertilmuth/being.svg?token=c2UbYMyGmqssmcwQYeAn&branch=main)](https://travis-ci.com/bertilmuth/being)
[![Gitter](https://badges.gitter.im/requirementsascode/community.svg)](https://gitter.im/requirementsascode/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)

The goal of Being is to maximize developer joy and productivity when building message-driven, event-sourced services.

By using a convention-over-configuration approach, Being reduces the number of concepts to learn. That enables you to focus on building domain logic. And whenever possible, the Being API uses POJOs. 

Being is based on the [Lagom framework](https://www.lagomframework.com/).
Here's Lagom's statement on the [advantages of event sourcing](https://www.lagomframework.com/documentation/1.6.x/java/ESAdvantage.html).

*This project has just recently been made public. The API is likely to change.*

# Getting started
The easiest way to get started is by [cloning the samples](https://github.com/bertilmuth/being-samples), and adapting them.
You need to define:
* The service API and implementation
* The aggregate and its event-sourced behavior
* A few configuration settings

Then you can run your service.

If you don't know what an aggregate is, please read the [following description](https://www.martinfowler.com/bliki/DDD_Aggregate.html).

An aggregate root entity and its contained elements constitute the state of the service.

You can find a runnable sample project containing the code below [here](https://github.com/bertilmuth/being-samples/tree/main/greetuser).

# The service API and implementation
## The service API
Let's define the service interface for a simple service that responds 
to a GET request with a greeting.
  
``` java
public interface GreetUserService extends AggregateService {  
  @Override
  default String id() {
    return "GreetUserService";
  }
  
  @Override
  default String address() {
    return "/api/greet/:id";
  }
  
  @Override
  default List<Class<?>> commandTypes() {
    return asList(ChangeGreetingText.class);
  }
  
  @Override
  default List<Class<?>> responseTypes() {
    return asList(GreetingResponse.class);
  }
}
```

### GETting information about the aggregate
To receive a greeting, you send a GET request to the address defined in the service interface, 
replacing the `:id` part with the id of the aggregate that you want to contact. For example:

Unix: `curl http://localhost:9000/api/greet/Joe`

Windows (PowerShell): `iwr http://localhost:9000/api/greet/Joe`

In this example, the aggregate id is used for the name in the greeting, so the response will be *Hello, Joe!*.

Where does the response *Hello, Joe!* come from? It's defined in the `responseToGet()` method
of your aggregate's behavior in the service implementation. [Take a peek](https://github.com/bertilmuth/being-samples/blob/main/greetuser/greetuser-impl/src/main/java/org/requirementsascode/being/greetuser/impl/GreetUserBehavior.java), if you want to.
We'll come back to it.

The class of the response must also be listed in the `responseTypes()`.

### POSTing a command to the aggregate
To change *Hello* do a different greeting, you need to send a POST request to the same address. Its JSON body must contain a `@type` property with the simple class name of a command, e.g. `ChangeGreetingText`. All commands must be listed by the `commandTypes()` method of the service interface. 

Example: To change the greeting text from *Hello, Joe!* to *Hi, Joe!*, send the following POST request:

Unix: `curl -H "Content-Type: application/json" -X POST -d '{"@type": "ChangeGreetingText", "newText":"Hi"}' http://localhost:9000/api/greet/Joe`

Windows (PowerShell): `iwr http://localhost:9000/api/greet/Joe -Method 'POST' -Headers @{'Content-Type' = 'application/json'} -Body '{"@type": "ChangeGreetingText", "newText":"Hi"}'`

Each POST request is processed by the `commandHandlers()` of the [aggregate behavior](https://github.com/bertilmuth/being-samples/blob/main/greetuser/greetuser-impl/src/main/java/org/requirementsascode/being/greetuser/impl/GreetUserBehavior.java).
The persisted events cause the aggregate state to change. Further GET requests return the new greeting.

### Commands & responses
Commands and responses are simple POJOs. They need to be serializable to JSON with the [Jackson](https://github.com/FasterXML/jackson) library.
The most concise way to define a command or response is to use [Lombok](https://projectlombok.org/)'s `@Value` annotation
that creates an all arguments constructor, getters for the fields, `equals()` and `hashCode()` methods etc.
Being's `@Properties` annotation makes sure the object is serialized correctly.

Command example:

``` java
@Value @Properties
public class ChangeGreetingText{
  String newText;
}
```

Response example:

``` java
@Value @Properties
public class GreetingResponse{
  String text;
}
```

If you don't want to use Lombok, and prefer hand-written POJOs, 
either:
* Provide a private, no-arguments constructor, or
* Mark the constructor with the `@JsonCreator` annotation.

## The service implementation
The service implementation defines the aggregate root class,
and creates the aggregate behavior that's resonsible for handling the incoming messages.

``` java
class GreetUserServiceImpl extends AggregateServiceImpl<Greeting> implements GreetUserService{
  @Override
  public Class<Greeting> aggregateRootClass() {
    return Greeting.class;
  }

  @Override
  public AggregateBehavior<Greeting> aggregateBehavior() {
    return new GreetUserBehavior();
  }
}
```

# The aggregate and its event-sourced behavior
## Aggregate root
An aggregate root, together with its contained elements,
represents the state of the service. It needs to be serializable to JSON as well,
for snapshots to be taken of the aggregate state from time to time.

``` java
@EqualsAndHashCode
class Greeting{
  private final String id;
  private final String text;
  private final String timestamp;
  
  private Greeting(String id, String text, String timestamp) {
    this.id = id;
    this.text = text;
    this.timestamp = timestamp;
  }

  public static Greeting create(String id, String text) {
    return new Greeting(id, text, LocalDateTime.now().toString());
  }
  
  public String getId() {
    return id;
  }

  public String getText() {
    return text;
  }

  public String getTimestamp() {
    return timestamp;
  }
}
```
## Aggregate behavior (event sourced)
The aggregate behavior defines how the service reacts to incoming messages.

The `createAggregateRoot()` creates the initial instance of the aggregate root,
before any messages have been processed.

The `responseToGet()` method creates a response to a GET request.

The `commandHandlers()` handle incoming commands and publish service internal events.
Being transparently persists these events, by default to [Apache Cassandra](https://cassandra.apache.org/).

The `internalEventHandlers()` handle each event, and publish an updated version of the aggregate root.

Use `.system()` instead of `.systemPublish` for mutuable state (see the [counter sample](https://github.com/bertilmuth/being-samples/blob/main/counter/counter-impl/src/main/java/org/requirementsascode/being/counter/impl/CounterBehavior.java)'s behavior as example).

``` java
class GreetUserBehavior extends AggregateBehavior<Greeting>{
  @Override
  public Greeting createAggregateRoot(String aggregateId) {
    return Greeting.create(aggregateId, "Hello");
  }
  
  @Override
  public Object responseToGet() {
    return new GreetingResponse(aggregateRoot().getText() + ", " + aggregateRoot().getId() + "!");
  }
  
  @Override
  public Model commandHandlers() {
    Model model = Model.builder()
      .user(ChangeGreetingText.class).systemPublish(this::greetingTextChanged)
      .build();
    return model;
  }
  
  @Override
  public Model internalEventHandlers() {
    Model model = Model.builder()
      .on(GreetingTextChanged.class).systemPublish(this::newGreeting)
      .build();
    return model;
  }
  
  private GreetingTextChanged greetingTextChanged(ChangeGreetingText command) {
    String newText = command.getNewText();
    if(newText.isEmpty()) {
      throw new RuntimeException("Text must not be empty!");
    }
    
    GreetingTextChanged event = new GreetingTextChanged(newText);
    return event;
  }
  
  private Greeting newGreeting(GreetingTextChanged event) {
    return Greeting.create(aggregateRoot().getId(), event.getText()); 
  }
  
  // Internal event classes
  
  @Value @Properties
  static final class GreetingTextChanged{
    String text;
  }
}
```


