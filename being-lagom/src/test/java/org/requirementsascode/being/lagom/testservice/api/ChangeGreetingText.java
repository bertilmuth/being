package org.requirementsascode.being.lagom.testservice.api;


import org.requirementsascode.being.*;

import lombok.Value;

@Value @Properties
public class ChangeGreetingText{
  String newText;
}
