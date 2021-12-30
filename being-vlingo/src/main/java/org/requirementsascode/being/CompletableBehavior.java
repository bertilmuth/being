package org.requirementsascode.being;

import io.vlingo.xoom.common.Completes;

public interface CompletableBehavior<STATE> {
	Completes<STATE> reactTo(Object message);
}
