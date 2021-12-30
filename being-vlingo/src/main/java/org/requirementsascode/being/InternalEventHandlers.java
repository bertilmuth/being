package org.requirementsascode.being;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

class InternalEventHandlers<STATE> extends Handlers<STATE>{
  private AggregateBehavior<STATE> aggregateBehavior;

  private InternalEventHandlers(AggregateBehavior<STATE> aggregateBehavior) {
    super(aggregateBehavior.eventHandlers());
    setAggregateBehavior(aggregateBehavior);
  }

  public static <STATE> InternalEventHandlers<STATE> fromBehavior(AggregateBehavior<STATE> aggregateBehavior) {
    return new InternalEventHandlers<STATE>(requireNonNull(aggregateBehavior, "aggregateBehavior must be non-null"));
  }

  @SuppressWarnings("unchecked")
  public Optional<Object> reactTo(Object internalEvent) {
    Optional<Object> publishedResult = handlers().reactTo(internalEvent);
    publishedResult.ifPresent(result -> {
      if (!isInstanceOfAggregateRootClass(result)) {
        throwIllegalSystemPublish(result);
      }
      aggregateBehavior().setState((STATE) result);
    });
    return publishedResult;
  }

  private boolean isInstanceOfAggregateRootClass(Object publishedResult) {
    Class<?> aggregateRootClass = aggregateBehavior().state().getClass();
    Class<? extends Object> publishedResultClass = publishedResult.getClass();
    return aggregateRootClass.equals(publishedResultClass);
  }

  private void throwIllegalSystemPublish(Object publishedResult) {
    Class<?> aggregateRootClass = aggregateBehavior().state().getClass();
    throw new AggregateBehavior.IllegalSystemPublish(
        ".systemPublish() of internal event handler should return " + aggregateRootClass.getSimpleName()
            + " instance, but did return " + publishedResult.getClass().getSimpleName() + " instance.");
  }

  private AggregateBehavior<STATE> aggregateBehavior() {
    return aggregateBehavior;
  }

  private void setAggregateBehavior(AggregateBehavior<STATE> aggregateBehavior) {
    this.aggregateBehavior = aggregateBehavior;
  }
}