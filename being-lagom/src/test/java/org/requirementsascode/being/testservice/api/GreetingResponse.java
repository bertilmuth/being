package org.requirementsascode.being.testservice.api;

import org.requirementsascode.being.*;

import lombok.Value;

@Value @Properties
public class GreetingResponse{
  String text;
}