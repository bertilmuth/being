package org.requirementsascode.being;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;

import akka.actor.typed.ActorRef;

@SuppressWarnings("serial")
class IncomingMessage extends JsonMessage {
  public final String id;
  public final ActorRef<?> replyTo;

  @JsonCreator(mode = Mode.PROPERTIES)
  public IncomingMessage(String id, Object payload, ActorRef<?> replyTo) {
    super(payload);
    this.id = id;
    this.replyTo = replyTo;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((replyTo == null) ? 0 : replyTo.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    IncomingMessage other = (IncomingMessage) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (replyTo == null) {
      if (other.replyTo != null)
        return false;
    } else if (!replyTo.equals(other.replyTo))
      return false;
    return true;
  }
}