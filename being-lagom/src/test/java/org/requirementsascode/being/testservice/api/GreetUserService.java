package org.requirementsascode.being.testservice.api;

import static java.util.Arrays.*;
import java.util.List;

import org.requirementsascode.being.AggregateService;
import org.requirementsascode.being.testservice.api.command.ChangeGreetingText;
import org.requirementsascode.being.testservice.api.command.FailingUpdateAggregateRoot;
import org.requirementsascode.being.testservice.api.command.IgnoredCommand;
import org.requirementsascode.being.testservice.api.command.IgnoredUpdateAggregateRoot;
import org.requirementsascode.being.testservice.api.command.PublishChangeGreetingTextList;
import org.requirementsascode.being.testservice.api.command.PublishChangeGreetingTextSet;
import org.requirementsascode.being.testservice.api.response.GreetingResponse;

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
    return asList(ChangeGreetingText.class, PublishChangeGreetingTextList.class, PublishChangeGreetingTextSet.class,
        IgnoredCommand.class, IgnoredUpdateAggregateRoot.class,
        FailingUpdateAggregateRoot.class);
  }

  @Override
  default List<Class<?>> responseTypes() {
    return asList(GreetingResponse.class,
        // Duplicate class ChangeGreetingText, only for testing purposes,+
        // DON'T DO THAT IN YOUR PROJECTS
        ChangeGreetingText.class);
  }
}
