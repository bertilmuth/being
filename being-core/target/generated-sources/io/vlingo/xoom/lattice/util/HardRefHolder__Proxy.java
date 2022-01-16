package io.vlingo.xoom.lattice.util;

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
import io.vlingo.xoom.lattice.util.HardRefHolder;
import java.lang.Object;

public class HardRefHolder__Proxy extends ActorProxyBase<io.vlingo.xoom.lattice.util.HardRefHolder> implements io.vlingo.xoom.lattice.util.HardRefHolder, Proxy {

  private static final String holdOnToRepresentation1 = "holdOnTo(java.lang.Object)";

  private final Actor actor;
  private final Mailbox mailbox;

  public HardRefHolder__Proxy(final Actor actor, final Mailbox mailbox) {
    super(io.vlingo.xoom.lattice.util.HardRefHolder.class, SerializationProxy.from(actor.definition()), actor.address());
    this.actor = actor;
    this.mailbox = mailbox;
  }

  public HardRefHolder__Proxy() {
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
    return "HardRefHolder[address=" + actor.address() + "]";
  }


  public void holdOnTo(java.lang.Object arg0) {
    if (!actor.isStopped()) {
      ActorProxyBase<HardRefHolder> self = this;
      final SerializableConsumer<HardRefHolder> consumer = (actor) -> actor.holdOnTo(ActorProxyBase.thunk(self, (Actor)actor, arg0));
      if (mailbox.isPreallocated()) { mailbox.send(actor, HardRefHolder.class, consumer, null, holdOnToRepresentation1); }
      else { mailbox.send(new LocalMessage<HardRefHolder>(actor, HardRefHolder.class, consumer, holdOnToRepresentation1)); }
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, holdOnToRepresentation1));
    }
  }
}
