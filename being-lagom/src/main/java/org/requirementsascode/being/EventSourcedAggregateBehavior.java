package org.requirementsascode.being;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import akka.actor.typed.ActorRef;
import akka.cluster.sharding.typed.javadsl.EntityContext;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.CommandHandlerWithReply;
import akka.persistence.typed.javadsl.CommandHandlerWithReplyBuilder;
import akka.persistence.typed.javadsl.EventHandler;
import akka.persistence.typed.javadsl.EventHandlerBuilder;
import akka.persistence.typed.javadsl.EventSourcedBehaviorWithEnforcedReplies;
import akka.persistence.typed.javadsl.ReplyEffect;

class EventSourcedAggregateBehavior<T> extends EventSourcedBehaviorWithEnforcedReplies<JsonMessage, EventContainer, AggregateState> {
  private final EntityContext<JsonMessage> entityContext;
  private final AggregateBehavior<T> aggregateBehavior;
  private final CommandHandlers<T> commandHandlers;
  private final InternalEventHandlers<T> internalEventHandlers;

  EventSourcedAggregateBehavior(EntityContext<JsonMessage> entityContext, AggregateBehavior<T> aggregateBehavior) {
    super(PersistenceId.of(entityContext.getEntityTypeKey().name(), entityContext.getEntityId()));
    this.entityContext = requireNonNull(entityContext, "entityContext must be non-null");
    this.aggregateBehavior = requireNonNull(aggregateBehavior, "aggregateBehavior must be non-null");

    this.commandHandlers = CommandHandlers.fromBehavior(aggregateBehavior);
    this.internalEventHandlers = InternalEventHandlers.fromBehavior(aggregateBehavior);
  }

  public static <T> EventSourcedAggregateBehavior<T> create(
      EntityContext<JsonMessage> entityContext, AggregateBehavior<T> aggregateBehavior) {
    return new EventSourcedAggregateBehavior<>(entityContext, aggregateBehavior);
  }

  @Override
  public AggregateState emptyState() {
    T aggregateRoot = aggregateBehavior.createAggregateRoot(entityContext.getEntityId());
    return new AggregateState(aggregateRoot);
  }

  @SuppressWarnings("unchecked")
  @Override
  public CommandHandlerWithReply<JsonMessage, EventContainer, AggregateState> commandHandler() {

    CommandHandlerWithReplyBuilder<JsonMessage, EventContainer, AggregateState> builder = newCommandHandlerWithReplyBuilder();

    builder.forAnyState().onCommand(IncomingMessage.class, (aggregateState, message) -> {
      aggregateBehavior.setAggregateRoot((T)aggregateState.aggregateRoot());
      return handleincomingMessage(message);
    });

    return builder.build();

  }

  private ReplyEffect<EventContainer, AggregateState> handleincomingMessage(IncomingMessage incomingMessage) {
    Object message = incomingMessage.payload();

    if(message instanceof GetRequest) {
      return handleGetRequest(incomingMessage);
    } else{
      return handlePostRequest(incomingMessage);
    }
  }
  
  @SuppressWarnings("unchecked")
  private ReplyEffect<EventContainer, AggregateState> handleGetRequest(IncomingMessage incomingMessage) {
    ActorRef<JsonMessage> replyTo = (ActorRef<JsonMessage>) incomingMessage.replyTo;
    return Effect().none().thenReply(replyTo, aggregateRoot -> new JsonMessage(aggregateBehavior.responseToGet()));
  }

  @SuppressWarnings("unchecked")
  private ReplyEffect<EventContainer, AggregateState> handlePostRequest(IncomingMessage incomingMessage) {
    Object message = incomingMessage.payload();
    ActorRef<Jsonified> replyTo = (ActorRef<Jsonified>)incomingMessage.replyTo;

    Optional<Object> optionalInternalEvent = null;
    try {
      optionalInternalEvent = commandHandlers.reactTo(message);
    } catch (Exception e) {
      return rejectMessage(replyTo, e);
    }
    
    return persistAndAcceptMessage(replyTo, optionalInternalEvent);
  }

  private ReplyEffect<EventContainer, AggregateState> persistAndAcceptMessage(ActorRef<Jsonified> replyTo,
      Optional<Object> optionalInternalEvent) {
    final Accepted acceptMessage = new Accepted();
    return optionalInternalEvent
      .map(event -> new EventContainer(event))
      .map(event -> Effect().persist(event).thenReply(replyTo, __ -> acceptMessage))
      .orElse(Effect().none().thenReply(replyTo, __ -> acceptMessage));
  }

  private ReplyEffect<EventContainer, AggregateState> rejectMessage(ActorRef<Jsonified> replyTo, Exception e) {
    return Effect().none().thenReply(replyTo, __ -> new Rejected(e.getMessage()));
  }
 
  @SuppressWarnings("unchecked")
  @Override
  public EventHandler<AggregateState, EventContainer> eventHandler() {
    EventHandlerBuilder<AggregateState, EventContainer> builder = newEventHandlerBuilder();

    builder.forAnyState().onEvent(EventContainer.class, (aggregateState, eventContainer) -> {
      T aggregateRoot = (T)aggregateState.aggregateRoot();
      aggregateBehavior.setAggregateRoot(aggregateRoot);
      Object event = eventContainer.event();
      Object newAggregateRoot = internalEventHandlers.reactTo(event).orElse(aggregateRoot);
      return new AggregateState(newAggregateRoot);
    });
    return builder.build();
  }
}