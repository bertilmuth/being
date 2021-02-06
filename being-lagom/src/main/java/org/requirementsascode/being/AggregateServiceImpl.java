package org.requirementsascode.being;

import static java.util.Objects.requireNonNull;

import java.time.Duration;

import javax.inject.Inject;

import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.BadRequest;

import akka.Done;
import akka.NotUsed;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;


/**
 * <p>
 * Enables you to specify the implementation of an {@link AggregateService} interface.
 * You need to define which aggregate root and aggregate behavior the service uses.
 * </p>
 * <p>
 * Create a subclass of this class and provide an implementation for the
 * following methods: {@link #aggregateRootClass()}, {@link #aggregateBehavior()}.
 * </p>
 * 
 * @author b_muth
 *
 */
public abstract class AggregateServiceImpl<T> implements AggregateService{
  private final Duration askTimeout = Duration.ofSeconds(10);
  private ClusterSharding clusterSharding;
  private EntityTypeKey<JsonMessage> entityTypeKey;

  protected AggregateServiceImpl() {
  }
  
  /**
   * Don't override this method, unless you're absolutely sure what you're doing.
   */
  @Override
  public ServiceCall<NotUsed, JsonMessage> httpGet(String id) {
    return request -> {

      EntityRef<JsonMessage> ref = entityRefFor(id);

      return ref.ask(replyTo -> new IncomingMessage(id, new GetRequest(), replyTo), askTimeout());
    };
  }

  /**
   * Don't override this method, unless you're absolutely sure what you're doing.
   */
  @Override
  public ServiceCall<Object, Done> httpPost(String id) {
    return messagePayload -> {

      EntityRef<JsonMessage> ref = entityRefFor(id);

      return ref.ask(replyTo -> {
        IncomingMessage message = new IncomingMessage(id, messagePayload, replyTo);
        return message;
      }, askTimeout()).thenApply(confirmation -> {
        if (confirmation instanceof Accepted) {
          return Done.getInstance();
        } else {
          throw new BadRequest(((Rejected) confirmation).reason());
        }
      });
    };
  }

  private EntityRef<JsonMessage> entityRefFor(String id) {
    return clusterSharding().entityRefFor(entityTypeKey(), id);
  }
  
  /**
   * The root entity. Its state will be modified by applying events.
   * 
   * @return the class of the root entity.
   */
  protected abstract Class<T> aggregateRootClass();
  
  /**
   * The behavior of the aggregate, defined by the command it can receive and
   * the events it persists and applies to the aggregate root.
   * 
   * @return the aggregate (root) behavior
   */
  protected abstract AggregateBehavior<T> aggregateBehavior();

  public Duration askTimeout() {
    return askTimeout;
  }

  private ClusterSharding clusterSharding() {
    return clusterSharding;
  }
  @Inject
  void setClusterSharding(ClusterSharding clusterSharding) {
    this.clusterSharding = requireNonNull(clusterSharding, "clusterSharding must be non-null");
    clusterSharding.init(Entity.of(entityTypeKey(), ctx -> EventSourcedAggregateBehavior.create(ctx, aggregateBehavior())));
  }

  private EntityTypeKey<JsonMessage> entityTypeKey() {
    if (entityTypeKey == null) {
      entityTypeKey = EntityTypeKey.create(JsonMessage.class, id());
    }
    return entityTypeKey;
  }
}
