package org.requirementsascode.being;

import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;
import static java.util.Objects.requireNonNull;

class EventConsumer<STATE> {
	private final EventApplier<STATE> eventApplier;

	public EventConsumer(final EventApplier<STATE> eventApplier) {
		this.eventApplier = requireNonNull(eventApplier, "eventApplier must be non-null!");
	}

	public void consumeEvent(final IdentifiedDomainEvent event) {
		eventHandlers().reactTo(event, state()).ifPresent(this::setState);
	}

	private STATE state() {
		return eventApplier.state();
	}

	private void setState(final STATE state) {
		eventApplier.setState(state);
	}

	private EventHandlers<STATE> eventHandlers() {
		return eventApplier.eventHandlers();
	}
}
