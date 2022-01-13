package org.requirementsascode.being;

import io.vlingo.xoom.symbio.Source;

@FunctionalInterface
public interface Merge<EVENT extends Source<?>,DATA> {
	DATA merge(EVENT event,DATA previousData);
}
