package org.requirementsascode.being;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import org.requirementsascode.AbstractActor;
import org.requirementsascode.Actor;
import org.requirementsascode.Model;

abstract class Handlers<T> {
  private Actor handlers;

  protected Handlers(Model handlersModel) {
    Actor handlers = newHandlers(requireNonNull(handlersModel, "handlersModel must be non-null"));
    setHandlers(handlers);
    deactivatePublishingEvents(handlers);
  }
  
  public abstract Optional<Object> reactTo(Object message);

  private Actor newHandlers(Model handlersModel) {
    return new Actor().withBehavior(handlersModel);
  }
  
  private void setHandlers(Actor handlers) {
    this.handlers = handlers;
  }
  
  protected AbstractActor handlers() {
    return handlers;
  }
  
  private final void deactivatePublishingEvents(AbstractActor messageHandlers) {
    messageHandlers.getModelRunner().publishWith(ev -> {});
  }
}
