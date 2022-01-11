package org.requirementsascode.being;

public interface EventApplier<STATE> {
	EventHandlers<STATE> eventHandlers();
	void setState(STATE state);
}
