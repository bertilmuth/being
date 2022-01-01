package org.requirementsascode.being;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

class ReactingCommandHandlers<STATE> extends ReactingHandlers<STATE> {
  private ReactingCommandHandlers(AggregateBehavior<STATE> aggregateBehavior) {
    super(aggregateBehavior.commandHandlers());
  }

  public static <STATE> ReactingCommandHandlers<STATE> from(AggregateBehavior<STATE> aggregateBehavior) {
    return new ReactingCommandHandlers<STATE>(requireNonNull(aggregateBehavior, "aggregateBehavior must be non-null"));
  }

  public Optional<Object> reactTo(Object command) {
    Optional<Object> result = handlers().reactTo(command);
    return result;
  }
}