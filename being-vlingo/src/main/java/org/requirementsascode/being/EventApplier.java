package org.requirementsascode.being;

public interface EventApplier<STATE> {
	EventHandlers<STATE> eventHandlers();
	STATE state();
	void setState(STATE state);
}
