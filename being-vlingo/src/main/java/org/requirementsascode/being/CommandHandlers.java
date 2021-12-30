package org.requirementsascode.being;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

class CommandHandlers<STATE> extends Handlers<STATE> {
  private CommandHandlers(AggregateBehavior<STATE> aggregateBehavior) {
    super(aggregateBehavior.commandHandlers());
  }

  public static <STATE> CommandHandlers<STATE> fromBehavior(AggregateBehavior<STATE> aggregateBehavior) {
    return new CommandHandlers<STATE>(requireNonNull(aggregateBehavior, "aggregateBehavior must be non-null"));
  }

  public Optional<Object> reactTo(Object command) {
    Optional<Object> result = handlers().reactTo(command);
    return result;
  }
}