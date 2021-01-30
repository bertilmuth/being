package org.requirementsascode.being;

/**
 * Message that signals that the incoming message has been rejected.
 * 
 * @author b_muth
 *
 */
@SuppressWarnings("serial")
public class Rejected extends JsonMessage {  
  Rejected(String reason) {
    super(reason);
  }
  
  public String reason() {
    return (String)payload();
  }
}