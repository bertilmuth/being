package org.requirementsascode.being.testservice.api;

import static java.util.Arrays.*;
import java.util.List;

import org.requirementsascode.being.AggregateService;

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
    return asList(ChangeGreetingText.class, PublishChangeGreetingTextList.class, PublishChangeGreetingTextSet.class,
        IgnoredCommand.class, TestFailingUpdateAggregateRootCommand.class);
  }

  @Override
  default List<Class<?>> outgoingMessageTypes() {
    return asList(GreetingResponse.class,
        // Duplicate class ChangeGreetingText, only for testing purposes,+
        // DON'T DO THAT IN YOUR PROJECTS
        ChangeGreetingText.class);
  }
}
