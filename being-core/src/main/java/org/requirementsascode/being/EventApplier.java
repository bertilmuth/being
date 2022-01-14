package org.requirementsascode.being;

interface EventApplier<STATE> {
	EventHandlers<STATE> eventHandlers();

	STATE state();

	void setState(STATE state);
}
