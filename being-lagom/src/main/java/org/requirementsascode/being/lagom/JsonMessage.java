package org.requirementsascode.being.lagom;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;

@SuppressWarnings("serial")
public class JsonMessage implements Jsonified{
  private final Object payload;
  
  @JsonCreator(mode = Mode.PROPERTIES)
  public JsonMessage(Object payload) {
    this.payload = requireNonNull(payload, "payload must be non-null");
  }
  
  public Object payload() {
    return payload;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((payload == null) ? 0 : payload.hashCode());
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
    JsonMessage other = (JsonMessage) obj;
    if (payload == null) {
      if (other.payload != null)
        return false;
    } else if (!payload.equals(other.payload))
      return false;
    return true;
  }
}
