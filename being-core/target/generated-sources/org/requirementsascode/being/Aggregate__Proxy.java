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
import org.requirementsascode.being.Aggregate;
import java.lang.Object;
import io.vlingo.xoom.common.Completes;

public class Aggregate__Proxy<I, O> extends ActorProxyBase<org.requirementsascode.being.Aggregate> implements org.requirementsascode.being.Aggregate<I, O>, Proxy {

  private static final String reactToRepresentation1 = "reactTo(I)";

  private final Actor actor;
  private final Mailbox mailbox;

  public Aggregate__Proxy(final Actor actor, final Mailbox mailbox) {
    super(org.requirementsascode.being.Aggregate.class, SerializationProxy.from(actor.definition()), actor.address());
    this.actor = actor;
    this.mailbox = mailbox;
  }

  public Aggregate__Proxy() {
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
    return "Aggregate[address=" + actor.address() + "]";
  }


  public io.vlingo.xoom.common.Completes<O> reactTo(I input) {
    if (!actor.isStopped()) {
      ActorProxyBase<Aggregate> self = this;
      final SerializableConsumer<Aggregate> consumer = (actor) -> actor.reactTo(ActorProxyBase.thunk(self, (Actor)actor, input));
      final io.vlingo.xoom.common.Completes<O> returnValue = Completes.using(actor.scheduler());
      if (mailbox.isPreallocated()) { mailbox.send(actor, Aggregate.class, consumer, Returns.value(returnValue), reactToRepresentation1); }
      else { mailbox.send(new LocalMessage<Aggregate>(actor, Aggregate.class, consumer, Returns.value(returnValue), reactToRepresentation1)); }
      return returnValue;
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, reactToRepresentation1));
    }
    return null;
  }
}
