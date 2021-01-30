package org.requirementsascode.being.lagom;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventShards;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTagger;
import com.lightbend.lagom.serialization.CompressedJsonable;

@SuppressWarnings("serial")
class EventContainer implements CompressedJsonable, AggregateEvent<EventContainer> {
  /**
   * Tags are used for getting and publishing streams of events. Each event will
   * have this tag, and in this case, we are partitioning the tags into 4 shards,
   * which means we can have 4 concurrent processors/publishers of events.
   */
  private static final AggregateEventShards<EventContainer> TAG = AggregateEventTag.sharded(EventContainer.class, 4);
  
  @JsonTypeInfo(use = Id.CLASS)
  private Object event;
  
  @JsonCreator
  EventContainer(Object event) {
    this.event = requireNonNull(event);
  }
  
  @Override
  public AggregateEventTagger<EventContainer> aggregateTag() {
    return TAG;
  }

  public Object event() {
    return event;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((event == null) ? 0 : event.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    EventContainer other = (EventContainer) obj;
    if (event == null) {
      if (other.event != null)
        return false;
    } else if (!event.equals(other.event))
      return false;
    return true;
  }
}

