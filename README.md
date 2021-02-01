# being
[![Gitter](https://badges.gitter.im/requirementsascode/community.svg)](https://gitter.im/requirementsascode/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
[![Build Status](https://travis-ci.com/bertilmuth/being.svg?token=c2UbYMyGmqssmcwQYeAn&branch=main)](https://travis-ci.com/bertilmuth/being)

The goal of being is to maximize developer joy and productivity when building message-driven, event-sourced services.

The easiest way to get started is by [cloning a sample project](https://github.com/bertilmuth/being-samples).

Being is based on the [Lagom framework](https://www.lagomframework.com/).

# code examples
You can find a runnable sample project containing the code [here](https://github.com/bertilmuth/being-samples/tree/main/greetuser).

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

# command
``` java
@Value @Properties
public class ChangeGreetingText{
  String newText;
}
```
# response
``` java
@Value @Properties
public class GreetingResponse{
  String text;
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
# aggregate root (POJO)
``` java
@Value
class Greeting{
  String id;
  String text;
  String timestamp;

  public static Greeting create(String id, String text) {
    return new Greeting(id, text, LocalDateTime.now().toString());
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
      .on(GreetingTextChanged.class).systemPublish(gtc -> Greeting.create(aggregateRoot().getId(), gtc.getText()))
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


