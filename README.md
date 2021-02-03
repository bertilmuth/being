# Being
[![Build Status](https://travis-ci.com/bertilmuth/being.svg?token=c2UbYMyGmqssmcwQYeAn&branch=main)](https://travis-ci.com/bertilmuth/being)
[![Gitter](https://badges.gitter.im/requirementsascode/community.svg)](https://gitter.im/requirementsascode/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)

The goal of Being is to maximize developer joy and productivity when building message-driven, event-sourced services.

This is manifested in the following principles:
* **Reduce cognitive load.** By using a convention-over-configuration approach, Being reduces the number of concepts to learn. There's a clear separation between infrastructure and domain logic, enabling you to be productive. And whenever possible, the Being API uses POJOs. 
* **Avoid concurrency issues by design.** Even if multiple users access the same aggregate instance, no concurrency issues will occur. Instead of relying on optimistic locking, Being manages each aggregate instance as an actor with its own inbox. Concurrency issues are avoided before they occur.
* **Make immutability optional.** You can choose whether you create a new aggregate instance when processing an event, or mutate the existing one. Both options are equally simple to implement.
* **Support fast and isolated behavior tests.** The aggregate behavior can be tested without the underlying framework, using synchronous calls. Of course, you can also test the service as a whole, by sending GET and POST requests to it.

Being is based on the [Lagom framework](https://www.lagomframework.com/).

# Getting started
The easiest way to get started is by [cloning the samples](https://github.com/bertilmuth/being-samples), and adapting them.
You need:
* A service interface and implementation
* An aggregate and its event-sourced behavior
* A few configuration settings
Then you can run your service.

You can find a runnable sample project containing the code below [here](https://github.com/bertilmuth/being-samples/tree/main/greetuser).

# Service interface and implementation
## Service interface
Let's define the service interface for a simple service that responds 
to a GET request with the greeting *Hello, Joe!*

You can change the name with every GET request.
You can also change *Hello* to a different greeting with a POST request,
and the service will remember that greeting associated with the name.
  
``` java
public interface GreetUserService extends AggregateService {  
  @Override
  default String uniqueName() {
    return "GreetUserService";
  }
  
  @Override
  default String address() {
    return "/api/greet/:id";
  }
  
  @Override
  default List<Class<?>> incomingMessageTypes() {
    return asList(ChangeGreetingText.class);
  }
  
  @Override
  default List<Class<?>> outgoingMessageTypes() {
    return asList(GreetingResponse.class);
  }
}
```

### GETting information about the aggregate
To receive a greeting, you send a GET request to the address defined in the service interface, for example:

Unix: `curl http://localhost:9000/api/greet/Joe`

Windows (PowerShell): `iwr http://localhost:9000/api/greet/Joe`

As you can see, the `address()` method defines the relative URL of the GET request.
The `:id` part of the URL will be replaced by the string that you supplied in your GET request.
That's how you can change the name. 

Where does the response *Hello, Joe!* come from? It's defined in the `responseMessage()` method
of your aggregate's behavior in the service implementation. [Take a peek](https://github.com/bertilmuth/being-samples/blob/main/greetuser/greetuser-impl/src/main/java/org/requirementsascode/being/greetuser/impl/GreetUserBehavior.java), if you want to.
We'll come back to it.

The class of the response must also be returned by the `outgoingMessageTypes()`.

### POSTing a command to the aggregate
To change *Hello* do a different greeting, you need to send a POST request to the same address. Its JSON body must contain a `@type` property with the simple class name of a command, e.g. `ChangeGreetingText`. All commands must be listed by the `incomingMessageTypes()` method of the service interface. 

Example: To change the greeting text from *Hello, Joe!* to *Hi, Joe!*, send the following POST request:

Unix: `curl -H "Content-Type: application/json" -X POST -d '{"@type": "ChangeGreetingText", "newText":"Hi"}' http://localhost:9000/api/greet/Joe`

Windows (PowerShell): `iwr http://localhost:9000/api/greet/Joe -Method 'POST' -Headers @{'Content-Type' = 'application/json'} -Body '{"@type": "ChangeGreetingText", "newText":"Hi"}'`

Each POST request is processed by the `incomingMessageHandlers()` of the [aggregate behavior](https://github.com/bertilmuth/being-samples/blob/main/greetuser/greetuser-impl/src/main/java/org/requirementsascode/being/greetuser/impl/GreetUserBehavior.java).
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

## Service implementation
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

# Aggregate and its event-sourced behavior
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

The `responseMessage()`message creates a response to a GET request.

The `incomingMessageHandlers()` handle incoming commands and publish service internal events.
Being transparenly persists these events, by default to [Apache Cassandra](https://cassandra.apache.org/).

The `internalEventHandlers()` handle each event, and produce a new version of the aggregate root.
Use `.system()` instead of `.systemPublish` for mutuable state. In that case, no new version of the aggregate root is published.

``` java
class GreetUserBehavior extends AggregateBehavior<Greeting>{
  @Override
  public Greeting createAggregateRoot(String aggregateId) {
    return Greeting.create(aggregateId, "Hello");
  }
  
  @Override
  public Object responseMessage() {
    return new GreetingResponse(aggregateRoot().getText() + ", " + aggregateRoot().getId() + "!");
  }
  
  @Override
  public Model incomingMessageHandlers() {
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


