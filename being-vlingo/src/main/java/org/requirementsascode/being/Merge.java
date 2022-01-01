package org.requirementsascode.being;

import io.vlingo.xoom.symbio.Source;

@FunctionalInterface
public interface Merge<DATA, EVENT extends Source<?>> {
	DATA merge(DATA previousData, EVENT event);
}
