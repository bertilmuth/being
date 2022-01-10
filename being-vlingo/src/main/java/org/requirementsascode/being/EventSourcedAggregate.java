package org.requirementsascode.being;

/**
 * <p>
 * Core class for defining how an aggregate reacts to commands by producing events (that are persisted),
 * and how events are applied to an aggregate to change state.
 * </p>
 * @author b_muth
 *
 * @param <STATE> the state of the aggregate
 */
public abstract class EventSourcedAggregate<CMD, STATE>{
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
   * Defines the command handlers. A handler maps an incoming command to the event(s) to be persisted.
   * In the handlers you can call {@link #state}<code>()</code> to get access to the current state of the aggregate.
   * @return the command handlers
   */
  public abstract CommandHandlers<STATE, CMD> commandHandlers();
  
  /**
   * Defines the event handlers. A handler maps a persisted event to the new state of the
   * aggregate. In handler methods, you can call
   * {@link #state}<code>()</code> to get access to the current state of the aggregate.
   * @return the event handlers
   */
  public abstract EventHandlers<STATE> eventHandlers();
}