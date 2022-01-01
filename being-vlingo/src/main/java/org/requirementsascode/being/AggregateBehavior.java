package org.requirementsascode.being;

import org.requirementsascode.Model;

/**
 * <p>
 * Core class for defining how an aggregate reacts to commands,
 * and how events are persisted via event sourcing.
 * </p>
 * @author b_muth
 *
 * @param <STATE> the state of the aggregate
 */
public abstract class AggregateBehavior<STATE>{
  private STATE state;
  
  /**
   * Creates the very first state of the aggregate.
   * 
   * @param aggregateId the id of the aggregate to be created
   * @return the initial state of the aggregate
   */
  public abstract STATE initialState(String aggregateId);

  /**
   * Don't call this method yourself.
   * Called by the library to inform about updated state of the aggregate.
   * 
   * @param state the root object of the aggregate
   */
  void setState(STATE state) {
    this.state = state;
  }
  
  /**
   * Call this method to access the current state of the aggregate,
   * typically within a model or its referenced methods.
   * 
   * @return the state of the aggregate
   */
  public STATE state() {
    return state;
  }
 
  /**
   * Provide a model defining command handlers. A handler consumes an incoming command and publishes the internal event(s) to be persisted.
   * In the handler methods you can call {@link #state}<code>()</code> to get access to the current aggregate root instance.
   * 
   * Here's an example implementation:
   * <pre>
   * &#64;Override
   * public Model commandHandlers() {
   *   Model model = Model.builder()
   *    .user(ChangeGreetingText.class).systemPublish(this::greetingTextChanged)
   *    .user(PublishChangeGreetingTextList.class).systemPublish(this::publishChangeGreetingTextList)
   *    .user(PublishChangeGreetingTextSet.class).systemPublish(this::publishChangeGreetingTextSet)
   *   .build();
   *  return model;
   * }
   *  
   * private GreetingTextChanged greetingTextChanged(ChangeGreetingText command) {
   *   ...
   * }
   * </pre>
   * <code>user(..)</code> defines the class of an incoming command message to be handled.
   * <p>
   * <code>systemPublish(..)</code> defines the handler function that takes a message instance,
   * and returns a single internal event or a list of internal events to be persisted.
   * </p>
   * 
   * @return the model
   */
  public abstract Model commandHandlers();
  
  /**
   * Provide a model defining internal event handlers. A handler consumes a persisted internal event and (optionally) publishes a new aggregate
   * root instance. In handler methods, you can call
   * {@link #state}<code>()</code> to get access to the current aggregate root instance.
   * 
   * Here's an example implementation: <pre>
   * &#64;Override
   * public Model internalEventHandlers() {
   *   Model model = Model.builder()
   *     .on(GreetingTextChanged.class).systemPublish(gtc -&gt; Greeting.create(aggregateRoot().id, gtc.text))
   *     .build();
   *   return model;
   * }
   * </pre>
   * 
   * <code>on(..)</code> defines the class of an internal event to be handled.
   * The internal events are replayed from the persisted events, produced by {@link #commandHandlers()}.
   * <p>
   * <code>systemPublish(..)</code> defines the handler function the takes an event
   * instance, and returns a new aggregate root instance. Use this syntax if
   * your aggregate root class is immutable.
   * </p>
   * <p>
   * Alternatively, you can use <code>system(..)</code> to just consume the event.
   * Use this syntax if your aggregate root class is mutable.
   * </p>
   * 
   * @return the model
   */
  public abstract Model eventHandlers();
  
  /** 
   * Exception thrown by the library when a systemPublish() statement inside a AggregateBehavior's model
   * returns an invalid object (e.g. of the wrong type) 
   * 
   * @author b_muth
   *
   */
  @SuppressWarnings("serial")
  public static class IllegalEventHandlerResult extends IllegalArgumentException{
    public IllegalEventHandlerResult(String message) {
      super(message);
    }
  }
}