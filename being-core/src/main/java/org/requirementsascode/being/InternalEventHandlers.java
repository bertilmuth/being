package org.requirementsascode.being;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

class InternalEventHandlers<T> extends Handlers<T>{
  private AggregateBehavior<T> aggregateBehavior;

  private InternalEventHandlers(AggregateBehavior<T> aggregateBehavior) {
    super(aggregateBehavior.internalEventHandlers());
    setAggregateBehavior(aggregateBehavior);
  }

  public static <T> InternalEventHandlers<T> fromBehavior(AggregateBehavior<T> aggregateBehavior) {
    return new InternalEventHandlers<T>(requireNonNull(aggregateBehavior, "aggregateBehavior must be non-null"));
  }

  @SuppressWarnings("unchecked")
  public Optional<Object> reactTo(Object internalEvent) {
    Optional<Object> publishedResult = handlers().reactTo(internalEvent);
    publishedResult.ifPresent(result -> {
      if (!isInstanceOfAggregateRootClass(result)) {
        throwIllegalSystemPublish(result);
      }
      aggregateBehavior().setAggregateRoot((T) result);
    });
    return publishedResult;
  }

  private boolean isInstanceOfAggregateRootClass(Object publishedResult) {
    Class<?> aggregateRootClass = aggregateBehavior().aggregateRoot().getClass();
    Class<? extends Object> publishedResultClass = publishedResult.getClass();
    return aggregateRootClass.equals(publishedResultClass);
  }

  private void throwIllegalSystemPublish(Object publishedResult) {
    Class<?> aggregateRootClass = aggregateBehavior().aggregateRoot().getClass();
    throw new AggregateBehavior.IllegalSystemPublish(
        ".systemPublish() of internal event handler should return " + aggregateRootClass.getSimpleName()
            + " instance, but did return " + publishedResult.getClass().getSimpleName() + " instance.");
  }

  private AggregateBehavior<T> aggregateBehavior() {
    return aggregateBehavior;
  }

  private void setAggregateBehavior(AggregateBehavior<T> aggregateBehavior) {
    this.aggregateBehavior = aggregateBehavior;
  }
}
