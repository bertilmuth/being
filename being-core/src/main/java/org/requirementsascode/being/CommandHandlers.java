package org.requirementsascode.being;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

class CommandHandlers<T> extends Handlers<T> {
  private CommandHandlers(AggregateBehavior<T> aggregateBehavior) {
    super(aggregateBehavior.commandHandlers());
  }

  public static <T> CommandHandlers<T> fromBehavior(AggregateBehavior<T> aggregateBehavior) {
    return new CommandHandlers<T>(requireNonNull(aggregateBehavior, "aggregateBehavior must be non-null"));
  }

  public Optional<Object> reactTo(Object command) {
    Optional<Object> result = handlers().reactTo(command);
    return result;
  }
}
