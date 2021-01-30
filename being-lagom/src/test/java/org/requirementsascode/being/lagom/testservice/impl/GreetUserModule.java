package org.requirementsascode.being.lagom.testservice.impl;

import org.requirementsascode.being.lagom.AggregateModule;
import org.requirementsascode.being.lagom.testservice.api.GreetUserService;

public class GreetUserModule extends AggregateModule {
  @Override
  protected void configure() {
    bindService(GreetUserService.class, GreetUserServiceImpl.class);
  }
}
