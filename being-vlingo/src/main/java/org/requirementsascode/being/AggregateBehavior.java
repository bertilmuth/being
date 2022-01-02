package org.requirementsascode.being;

/**
 * <p>
 * Core class for defining how an entity reacts to commands by producing events (that are persisted),
 * and how events are applied to an entity to change state.
 * </p>
 * @author b_muth
 *
 * @param <STATE> the state of the entity
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
   * Defines the command handlers. A handler consumes an incoming command and produces the event(s) to be persisted.
   * In the handler methods you can call {@link #state}<code>()</code> to get access to the current state of the entity.
   * @return the model
   */
  public abstract CommandHandlers commandHandlers();
  
  /**
   * Defines the event handlers. A handler consumes a persisted event and (optionally) publishes a new state of the
   * entity. In handler methods, you can call
   * {@link #state}<code>()</code> to get access to the current state of the entity.
   * @return the model
   */
  public abstract EventHandlers<STATE> eventHandlers();
  
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