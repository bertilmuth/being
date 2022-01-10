package org.requirementsascode.being;

import io.vlingo.xoom.common.Completes;

public interface AsyncBehavior<I, O> {
	Completes<O> reactTo(I message);
}
