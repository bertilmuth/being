package org.requirementsascode.being.testservice.impl;

import org.requirementsascode.being.AggregateModule;
import org.requirementsascode.being.testservice.api.GreetUserService;

public class GreetUserModule extends AggregateModule {
  @Override
  protected void configure() {
    bindService(GreetUserService.class, GreetUserServiceImpl.class);
  }
}
