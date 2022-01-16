package io.vlingo.xoom.lattice.grid.application;

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
import io.vlingo.xoom.lattice.grid.application.GridActorControl.Outbound;
import io.vlingo.xoom.lattice.grid.application.message.Message;
import io.vlingo.xoom.wire.fdx.outbound.ApplicationOutboundStream;
import io.vlingo.xoom.wire.node.Id;
import java.lang.Object;
import io.vlingo.xoom.lattice.grid.Grid;
import io.vlingo.xoom.lattice.grid.application.message.Answer;
import io.vlingo.xoom.actors.Address;
import io.vlingo.xoom.common.SerializableConsumer;
import java.util.function.Function;
import java.lang.Class;
import io.vlingo.xoom.actors.Returns;
import io.vlingo.xoom.actors.Definition.SerializationProxy;
import java.util.List;
import java.lang.String;
import io.vlingo.xoom.actors.Actor;

public class GridActorControlOutbound__Proxy extends ActorProxyBase<io.vlingo.xoom.lattice.grid.application.GridActorControl.Outbound> implements io.vlingo.xoom.lattice.grid.application.GridActorControl.Outbound, Proxy {

  private static final String useStreamRepresentation1 = "useStream(io.vlingo.xoom.wire.fdx.outbound.ApplicationOutboundStream)";
  private static final String startRepresentation2 = "start(io.vlingo.xoom.wire.node.Id, io.vlingo.xoom.wire.node.Id, java.lang.Class<T>, io.vlingo.xoom.actors.Address, io.vlingo.xoom.actors.Definition.SerializationProxy)";
  private static final String informNodeIsHealthyRepresentation3 = "informNodeIsHealthy(io.vlingo.xoom.wire.node.Id, boolean)";
  private static final String gridDeliverRepresentation4 = "gridDeliver(io.vlingo.xoom.wire.node.Id, io.vlingo.xoom.wire.node.Id, io.vlingo.xoom.actors.Returns<?>, java.lang.Class<T>, io.vlingo.xoom.actors.Address, io.vlingo.xoom.actors.Definition.SerializationProxy, io.vlingo.xoom.common.SerializableConsumer<T>, java.lang.String)";
  private static final String actorDeliverRepresentation5 = "actorDeliver(io.vlingo.xoom.wire.node.Id, io.vlingo.xoom.wire.node.Id, io.vlingo.xoom.actors.Returns<?>, java.lang.Class<T>, java.util.function.Function<io.vlingo.xoom.lattice.grid.Grid, io.vlingo.xoom.actors.Actor>, io.vlingo.xoom.common.SerializableConsumer<T>, java.lang.String)";
  private static final String answerRepresentation6 = "answer(io.vlingo.xoom.wire.node.Id, io.vlingo.xoom.wire.node.Id, io.vlingo.xoom.lattice.grid.application.message.Answer<T>)";
  private static final String forwardRepresentation7 = "forward(io.vlingo.xoom.wire.node.Id, io.vlingo.xoom.wire.node.Id, io.vlingo.xoom.lattice.grid.application.message.Message)";
  private static final String relocateRepresentation8 = "relocate(io.vlingo.xoom.wire.node.Id, io.vlingo.xoom.wire.node.Id, io.vlingo.xoom.actors.Definition.SerializationProxy, io.vlingo.xoom.actors.Address, java.lang.Object, java.util.List<? extends io.vlingo.xoom.actors.Message>)";

  private final Actor actor;
  private final Mailbox mailbox;

  public GridActorControlOutbound__Proxy(final Actor actor, final Mailbox mailbox) {
    super(io.vlingo.xoom.lattice.grid.application.GridActorControl.Outbound.class, SerializationProxy.from(actor.definition()), actor.address());
    this.actor = actor;
    this.mailbox = mailbox;
  }

  public GridActorControlOutbound__Proxy() {
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
    return "Outbound[address=" + actor.address() + "]";
  }


  public void useStream(io.vlingo.xoom.wire.fdx.outbound.ApplicationOutboundStream arg0) {
    if (!actor.isStopped()) {
      ActorProxyBase<Outbound> self = this;
      final SerializableConsumer<Outbound> consumer = (actor) -> actor.useStream(ActorProxyBase.thunk(self, (Actor)actor, arg0));
      if (mailbox.isPreallocated()) { mailbox.send(actor, Outbound.class, consumer, null, useStreamRepresentation1); }
      else { mailbox.send(new LocalMessage<Outbound>(actor, Outbound.class, consumer, useStreamRepresentation1)); }
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, useStreamRepresentation1));
    }
  }

  public <T>void start(io.vlingo.xoom.wire.node.Id arg0, io.vlingo.xoom.wire.node.Id arg1, java.lang.Class<T> arg2, io.vlingo.xoom.actors.Address arg3, io.vlingo.xoom.actors.Definition.SerializationProxy arg4) {
    if (!actor.isStopped()) {
      ActorProxyBase<Outbound> self = this;
      final SerializableConsumer<Outbound> consumer = (actor) -> actor.start(ActorProxyBase.thunk(self, (Actor)actor, arg0), ActorProxyBase.thunk(self, (Actor)actor, arg1), ActorProxyBase.thunk(self, (Actor)actor, arg2), ActorProxyBase.thunk(self, (Actor)actor, arg3), ActorProxyBase.thunk(self, (Actor)actor, arg4));
      if (mailbox.isPreallocated()) { mailbox.send(actor, Outbound.class, consumer, null, startRepresentation2); }
      else { mailbox.send(new LocalMessage<Outbound>(actor, Outbound.class, consumer, startRepresentation2)); }
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, startRepresentation2));
    }
  }

  public void informNodeIsHealthy(io.vlingo.xoom.wire.node.Id arg0, boolean arg1) {
    if (!actor.isStopped()) {
      ActorProxyBase<Outbound> self = this;
      final SerializableConsumer<Outbound> consumer = (actor) -> actor.informNodeIsHealthy(ActorProxyBase.thunk(self, (Actor)actor, arg0), ActorProxyBase.thunk(self, (Actor)actor, arg1));
      if (mailbox.isPreallocated()) { mailbox.send(actor, Outbound.class, consumer, null, informNodeIsHealthyRepresentation3); }
      else { mailbox.send(new LocalMessage<Outbound>(actor, Outbound.class, consumer, informNodeIsHealthyRepresentation3)); }
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, informNodeIsHealthyRepresentation3));
    }
  }

  public <T>void gridDeliver(io.vlingo.xoom.wire.node.Id arg0, io.vlingo.xoom.wire.node.Id arg1, io.vlingo.xoom.actors.Returns<?> arg2, java.lang.Class<T> arg3, io.vlingo.xoom.actors.Address arg4, io.vlingo.xoom.actors.Definition.SerializationProxy arg5, io.vlingo.xoom.common.SerializableConsumer<T> arg6, java.lang.String arg7) {
    if (!actor.isStopped()) {
      ActorProxyBase<Outbound> self = this;
      final SerializableConsumer<Outbound> consumer = (actor) -> actor.gridDeliver(ActorProxyBase.thunk(self, (Actor)actor, arg0), ActorProxyBase.thunk(self, (Actor)actor, arg1), ActorProxyBase.thunk(self, (Actor)actor, arg2), ActorProxyBase.thunk(self, (Actor)actor, arg3), ActorProxyBase.thunk(self, (Actor)actor, arg4), ActorProxyBase.thunk(self, (Actor)actor, arg5), ActorProxyBase.thunk(self, (Actor)actor, arg6), ActorProxyBase.thunk(self, (Actor)actor, arg7));
      if (mailbox.isPreallocated()) { mailbox.send(actor, Outbound.class, consumer, null, gridDeliverRepresentation4); }
      else { mailbox.send(new LocalMessage<Outbound>(actor, Outbound.class, consumer, gridDeliverRepresentation4)); }
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, gridDeliverRepresentation4));
    }
  }

  public <T>void actorDeliver(io.vlingo.xoom.wire.node.Id arg0, io.vlingo.xoom.wire.node.Id arg1, io.vlingo.xoom.actors.Returns<?> arg2, java.lang.Class<T> arg3, java.util.function.Function<io.vlingo.xoom.lattice.grid.Grid, io.vlingo.xoom.actors.Actor> arg4, io.vlingo.xoom.common.SerializableConsumer<T> arg5, java.lang.String arg6) {
    if (!actor.isStopped()) {
      ActorProxyBase<Outbound> self = this;
      final SerializableConsumer<Outbound> consumer = (actor) -> actor.actorDeliver(ActorProxyBase.thunk(self, (Actor)actor, arg0), ActorProxyBase.thunk(self, (Actor)actor, arg1), ActorProxyBase.thunk(self, (Actor)actor, arg2), ActorProxyBase.thunk(self, (Actor)actor, arg3), ActorProxyBase.thunk(self, (Actor)actor, arg4), ActorProxyBase.thunk(self, (Actor)actor, arg5), ActorProxyBase.thunk(self, (Actor)actor, arg6));
      if (mailbox.isPreallocated()) { mailbox.send(actor, Outbound.class, consumer, null, actorDeliverRepresentation5); }
      else { mailbox.send(new LocalMessage<Outbound>(actor, Outbound.class, consumer, actorDeliverRepresentation5)); }
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, actorDeliverRepresentation5));
    }
  }

  public <T>void answer(io.vlingo.xoom.wire.node.Id arg0, io.vlingo.xoom.wire.node.Id arg1, io.vlingo.xoom.lattice.grid.application.message.Answer<T> arg2) {
    if (!actor.isStopped()) {
      ActorProxyBase<Outbound> self = this;
      final SerializableConsumer<Outbound> consumer = (actor) -> actor.answer(ActorProxyBase.thunk(self, (Actor)actor, arg0), ActorProxyBase.thunk(self, (Actor)actor, arg1), ActorProxyBase.thunk(self, (Actor)actor, arg2));
      if (mailbox.isPreallocated()) { mailbox.send(actor, Outbound.class, consumer, null, answerRepresentation6); }
      else { mailbox.send(new LocalMessage<Outbound>(actor, Outbound.class, consumer, answerRepresentation6)); }
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, answerRepresentation6));
    }
  }

  public void forward(io.vlingo.xoom.wire.node.Id arg0, io.vlingo.xoom.wire.node.Id arg1, io.vlingo.xoom.lattice.grid.application.message.Message arg2) {
    if (!actor.isStopped()) {
      ActorProxyBase<Outbound> self = this;
      final SerializableConsumer<Outbound> consumer = (actor) -> actor.forward(ActorProxyBase.thunk(self, (Actor)actor, arg0), ActorProxyBase.thunk(self, (Actor)actor, arg1), ActorProxyBase.thunk(self, (Actor)actor, arg2));
      if (mailbox.isPreallocated()) { mailbox.send(actor, Outbound.class, consumer, null, forwardRepresentation7); }
      else { mailbox.send(new LocalMessage<Outbound>(actor, Outbound.class, consumer, forwardRepresentation7)); }
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, forwardRepresentation7));
    }
  }

  public void relocate(io.vlingo.xoom.wire.node.Id arg0, io.vlingo.xoom.wire.node.Id arg1, io.vlingo.xoom.actors.Definition.SerializationProxy arg2, io.vlingo.xoom.actors.Address arg3, java.lang.Object arg4, java.util.List<? extends io.vlingo.xoom.actors.Message> arg5) {
    if (!actor.isStopped()) {
      ActorProxyBase<Outbound> self = this;
      final SerializableConsumer<Outbound> consumer = (actor) -> actor.relocate(ActorProxyBase.thunk(self, (Actor)actor, arg0), ActorProxyBase.thunk(self, (Actor)actor, arg1), ActorProxyBase.thunk(self, (Actor)actor, arg2), ActorProxyBase.thunk(self, (Actor)actor, arg3), ActorProxyBase.thunk(self, (Actor)actor, arg4), ActorProxyBase.thunk(self, (Actor)actor, arg5));
      if (mailbox.isPreallocated()) { mailbox.send(actor, Outbound.class, consumer, null, relocateRepresentation8); }
      else { mailbox.send(new LocalMessage<Outbound>(actor, Outbound.class, consumer, relocateRepresentation8)); }
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, relocateRepresentation8));
    }
  }
}
