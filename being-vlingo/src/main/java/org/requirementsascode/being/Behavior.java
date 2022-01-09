package org.requirementsascode.being;

import io.vlingo.xoom.common.Completes;

public interface Behavior<I, O> {
	Completes<O> reactTo(I message);
}
