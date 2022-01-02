package org.requirementsascode.being;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

class ReactingEventHandlers<STATE> extends ReactingHandlers<STATE>{
  private AggregateBehavior<STATE> aggregateBehavior;

  private ReactingEventHandlers(AggregateBehavior<STATE> aggregateBehavior) {
    super(aggregateBehavior.eventHandlers());
    setAggregateBehavior(aggregateBehavior);
  }

  public static <STATE> ReactingEventHandlers<STATE> of(AggregateBehavior<STATE> aggregateBehavior) {
    return new ReactingEventHandlers<STATE>(requireNonNull(aggregateBehavior, "aggregateBehavior must be non-null"));
  }

  @SuppressWarnings("unchecked")
  public Optional<Object> reactTo(Object event) {
    Optional<Object> result = super.reactTo(event);
    result.ifPresent(r -> {
      if (!isInstanceOfStateClass(r)) {
        throwEventHandlerResultIsNotState(r);
      }
      aggregateBehavior().setState((STATE) r);
    });
    return result;
  }

  private boolean isInstanceOfStateClass(Object publishedResult) {
    Class<?> aggregateRootClass = aggregateBehavior().state().getClass();
    Class<? extends Object> publishedResultClass = publishedResult.getClass();
    return aggregateRootClass.equals(publishedResultClass);
  }

  private void throwEventHandlerResultIsNotState(Object result) {
    Class<?> stateClass = aggregateBehavior().state().getClass();
    throw new AggregateBehavior.IllegalEventHandlerResult(
        "Event handler should return " + stateClass.getSimpleName()
            + " instance, but did return " + result.getClass().getSimpleName() + " instance.");
  }

  private AggregateBehavior<STATE> aggregateBehavior() {
    return aggregateBehavior;
  }

  private void setAggregateBehavior(AggregateBehavior<STATE> aggregateBehavior) {
    this.aggregateBehavior = aggregateBehavior;
  }
}