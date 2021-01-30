package org.requirementsascode.being.lagom.testservice.impl;

import org.requirementsascode.being.AggregateBehavior;
import org.requirementsascode.being.lagom.AggregateServiceImpl;
import org.requirementsascode.being.lagom.testservice.api.GreetUserService;

class GreetUserServiceImpl extends AggregateServiceImpl<Greeting> implements GreetUserService{
  @Override
  public Class<Greeting> aggregateRootClass() {
    return Greeting.class;
  }

  @Override
  public AggregateBehavior<Greeting> aggregateBehavior() {
    return new GreetUserBehavior();
  }
}
