package org.requirementsascode.being;

import static java.util.Objects.requireNonNull;

class ReactingCommandHandlers<STATE> extends ReactingHandlers<STATE> {
  private ReactingCommandHandlers(AggregateBehavior<STATE> aggregateBehavior) {
    super(aggregateBehavior.commandHandlers());
  }

  public static <STATE> ReactingCommandHandlers<STATE> from(AggregateBehavior<STATE> aggregateBehavior) {
    return new ReactingCommandHandlers<STATE>(requireNonNull(aggregateBehavior, "aggregateBehavior must be non-null"));
  }
}