package org.requirementsascode.being;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.lightbend.lagom.serialization.CompressedJsonable;

@SuppressWarnings("serial")
class AggregateState implements CompressedJsonable {
  @JsonTypeInfo(use = Id.CLASS)
  private Object aggregateRoot;
  
  @JsonCreator
  AggregateState(Object aggregateRoot){
    this.aggregateRoot = aggregateRoot; 
  }
  
  Object aggregateRoot() {
    return aggregateRoot;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((aggregateRoot == null) ? 0 : aggregateRoot.hashCode());
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
    AggregateState other = (AggregateState) obj;
    if (aggregateRoot == null) {
      if (other.aggregateRoot != null)
        return false;
    } else if (!aggregateRoot.equals(other.aggregateRoot))
      return false;
    return true;
  }
}
