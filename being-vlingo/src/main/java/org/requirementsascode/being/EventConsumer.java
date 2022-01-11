package org.requirementsascode.being;

import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;

class EventConsumer<STATE> {
	private final EventApplier<STATE> eventApplier;

	public EventConsumer(EventApplier<STATE> eventApplier) {
		this.eventApplier = eventApplier;
	}
	
	public void consumeEvent(IdentifiedDomainEvent event) {
		eventHandlers().reactTo(event, state())
			.ifPresent(this::setState);
	}

	private STATE state() {
		return eventApplier.state();
	}
	
	private void setState(STATE state) {
		eventApplier.setState(state);
	}
	
	private EventHandlers<STATE> eventHandlers(){
		return eventApplier.eventHandlers();
	}
}
