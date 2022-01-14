package org.requirementsascode.being;

import io.vlingo.xoom.common.Completes;

public interface Aggregate<I, O> {
	Completes<O> reactTo(I input);
}
