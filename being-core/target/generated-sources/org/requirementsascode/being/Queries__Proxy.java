package org.requirementsascode.being;

import io.vlingo.xoom.actors.Actor;
import io.vlingo.xoom.actors.ActorProxyBase;
import io.vlingo.xoom.actors.Address;
import io.vlingo.xoom.actors.DeadLetter;
import io.vlingo.xoom.actors.Definition.SerializationProxy;
import io.vlingo.xoom.actors.LocalMessage;
import io.vlingo.xoom.actors.Mailbox;
import io.vlingo.xoom.actors.Proxy;
import io.vlingo.xoom.actors.Returns;
import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.common.SerializableConsumer;
import org.requirementsascode.being.Queries;
import java.util.Collection;
import java.lang.Object;
import io.vlingo.xoom.common.Completes;
import java.lang.String;

public class Queries__Proxy<DATA> extends ActorProxyBase<org.requirementsascode.being.Queries> implements org.requirementsascode.being.Queries<DATA>, Proxy {

  private static final String findByIdRepresentation1 = "findById(java.lang.String)";
  private static final String findAllRepresentation2 = "findAll()";

  private final Actor actor;
  private final Mailbox mailbox;

  public Queries__Proxy(final Actor actor, final Mailbox mailbox) {
    super(org.requirementsascode.being.Queries.class, SerializationProxy.from(actor.definition()), actor.address());
    this.actor = actor;
    this.mailbox = mailbox;
  }

  public Queries__Proxy() {
    super();
    this.actor = null;
    this.mailbox = null;
  }


  public Address address() {
    return actor.address();
  }

  public boolean equals(final Object other) {
    if (this == other) return true;
    if (other == null) return false;
    if (other.getClass() != getClass()) return false;
    return address().equals(Proxy.from(other).address());
  }

  public int hashCode() {
    return 31 + getClass().hashCode() + actor.address().hashCode();
  }

  public String toString() {
    return "Queries[address=" + actor.address() + "]";
  }


  public io.vlingo.xoom.common.Completes<DATA> findById(java.lang.String id) {
    if (!actor.isStopped()) {
      ActorProxyBase<Queries> self = this;
      final SerializableConsumer<Queries> consumer = (actor) -> actor.findById(ActorProxyBase.thunk(self, (Actor)actor, id));
      final io.vlingo.xoom.common.Completes<DATA> returnValue = Completes.using(actor.scheduler());
      if (mailbox.isPreallocated()) { mailbox.send(actor, Queries.class, consumer, Returns.value(returnValue), findByIdRepresentation1); }
      else { mailbox.send(new LocalMessage<Queries>(actor, Queries.class, consumer, Returns.value(returnValue), findByIdRepresentation1)); }
      return returnValue;
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, findByIdRepresentation1));
    }
    return null;
  }

  public io.vlingo.xoom.common.Completes<java.util.Collection<DATA>> findAll() {
    if (!actor.isStopped()) {
      ActorProxyBase<Queries> self = this;
      final SerializableConsumer<Queries> consumer = (actor) -> actor.findAll();
      final io.vlingo.xoom.common.Completes<java.util.Collection<DATA>> returnValue = Completes.using(actor.scheduler());
      if (mailbox.isPreallocated()) { mailbox.send(actor, Queries.class, consumer, Returns.value(returnValue), findAllRepresentation2); }
      else { mailbox.send(new LocalMessage<Queries>(actor, Queries.class, consumer, Returns.value(returnValue), findAllRepresentation2)); }
      return returnValue;
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, findAllRepresentation2));
    }
    return null;
  }
}
