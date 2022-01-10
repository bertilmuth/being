package org.requirementsascode.being;

import io.vlingo.xoom.common.Completes;

public interface AggregateBehavior<I, O> {
	Completes<O> reactTo(I input);
}
