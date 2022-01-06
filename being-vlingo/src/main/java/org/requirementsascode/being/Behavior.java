package org.requirementsascode.being;

import io.vlingo.xoom.common.Completes;

public interface Behavior<STATE> {
	Completes<STATE> reactTo(Object message);
}
