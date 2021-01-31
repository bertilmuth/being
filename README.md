# being
[![Gitter](https://badges.gitter.im/requirementsascode/community.svg)](https://gitter.im/requirementsascode/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
[![Build Status](https://travis-ci.com/bertilmuth/being.svg?token=c2UbYMyGmqssmcwQYeAn&branch=main)](https://travis-ci.com/bertilmuth/being)

The goal of being is to maximize developer joy and productivity when building message-driven, event-sourced services.

Being is based on the [Lagom framework](https://www.lagomframework.com/).

# service interface
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
Example URL for GET/POST requests: 

http://localhost:9000/api/greet/Joe

GET request will get you the response defined by the aggregate behavior's `responseMessage()` method (see below).

Use POST requests for commands. The JSON must contain a `@type` property with simple class name of command, e.g. `ChangeGreetingText`.
Example JSON:

`{"@type":"ChangeGreetingText", "newText":"Guten Tag"}`

# command
``` java
@Value @Properties
public class ChangeGreetingText{
  String newText;
}
```
# service implementation
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
# aggregate behavior (event sourced)
``` java
class GreetUserBehavior extends AggregateBehavior<Greeting>{
  @Override
  public Greeting createAggregateRoot(String aggregateId) {
    return Greeting.create(aggregateId, "Hello");
  }
  
  @Override
  public Object responseMessage() {
    return new GreetingResponse(aggregateRoot().text + ", " + aggregateRoot().id + "!");
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
      .on(GreetingTextChanged.class).systemPublish(gtc -> Greeting.create(aggregateRoot().id, gtc.text))
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
  
  // Internal event classes
  @Value @Properties
  static final class GreetingTextChanged{
    String text;
  }
}
```
# aggregate root
``` java
@EqualsAndHashCode
class Greeting{
  public final String id;
  public final String text;
  public final String timestamp;

  private Greeting(String id, String text, String timestamp) {
    this.id = id;
    this.text = requireNonNull(text, "text must be non-null");
    this.timestamp = requireNonNull(timestamp, "timestamp must be non-null");
  }

  public static Greeting create(String id, String text) {
    return new Greeting(id, text, LocalDateTime.now().toString());
  }
}
```

