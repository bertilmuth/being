package org.requirementsascode.being;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

public class IncomingMessageHandlers<T> extends Handlers<T> {
  private IncomingMessageHandlers(AggregateBehavior<T> aggregateBehavior) {
    super(aggregateBehavior.incomingMessageHandlers());
  }

  public static <T> IncomingMessageHandlers<T> fromBehavior(AggregateBehavior<T> aggregateBehavior) {
    return new IncomingMessageHandlers<T>(requireNonNull(aggregateBehavior, "aggregateBehavior must be non-null"));
  }

  public Optional<Object> reactTo(Object incomingMessage) {
    Optional<Object> result = handlers().reactTo(incomingMessage);
    return result;
  }
}
