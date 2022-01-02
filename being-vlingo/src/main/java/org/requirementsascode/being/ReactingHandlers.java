package org.requirementsascode.being;

import java.util.Optional;

import org.requirementsascode.AbstractActor;
import org.requirementsascode.Actor;
import org.requirementsascode.Model;

import static java.util.Objects.requireNonNull;

abstract class ReactingHandlers<T> {
  private Actor handlers;

  protected ReactingHandlers(Model handlersModel) {
    Actor handlers = newHandlers(requireNonNull(handlersModel, "handlersModel must be non-null"));
    setHandlers(handlers);
    deactivatePublishingEvents(handlers);
  }
  
  public Optional<Object> reactTo(Object message){
	  return handlers.reactTo(message);
  }

  private Actor newHandlers(Model handlersModel) {
    return new Actor().withBehavior(handlersModel);
  }
  
  private void setHandlers(Actor handlers) {
    this.handlers = handlers;
  }
  
  private final void deactivatePublishingEvents(AbstractActor messageHandlers) {
    messageHandlers.getModelRunner().publishWith(ev -> {});
  }
}