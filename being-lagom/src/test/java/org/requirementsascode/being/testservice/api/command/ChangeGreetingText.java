package org.requirementsascode.being.testservice.api.command;


import org.requirementsascode.being.*;

import lombok.Value;

@Value @Properties
public class ChangeGreetingText{
  String newText;
}
