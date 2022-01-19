package org.requirementsascode.being;

/**
 * <p>
 * Core interface for defining how an aggregate reacts to commands by producing events (that are persisted),
 * and how events are applied to an aggregate to change state.
 * </p>
 * @author b_muth
 * 
 * @param <CMD>   the base type of all handled command types
 * @param <STATE> the state of the aggregate
 */
public interface AggregateBehavior<CMD, STATE>{  
  /**
   * Creates the very first state of the aggregate.
   * 
   * @param aggregateId the id of the aggregate to be created
   * @return the initial state of the aggregate
   */
  public abstract STATE initialState(String aggregateId);
 
  /**
   * Defines the command handlers. A command handler consumes an incoming command and produces the event(s) to be persisted.
   * @return the command handlers
   */
  public abstract CommandHandlers<CMD,STATE> commandHandlers();
  
  /**
   * Defines the event handlers. An event handler consumes a persisted event and produces the new state of the
   * aggregate. 
   * @return the event handlers
   */
  public abstract EventHandlers<STATE> eventHandlers();
}