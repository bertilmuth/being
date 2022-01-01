package org.requirementsascode.being;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import org.requirementsascode.Step;

import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;
import io.vlingo.xoom.lattice.model.sourcing.EventSourced;

public class EventSourcedAggregateBehavior<STATE> extends EventSourced implements CompletableBehavior<STATE> {
	private final AggregateBehavior<STATE> aggregateBehavior;
	private final ReactingCommandHandlers<STATE> reactingCommandHandlers;
	private final ReactingEventHandlers<STATE> reactingEventHandlers;

	public EventSourcedAggregateBehavior(String entityId, AggregateBehavior<STATE> aggregateBehavior) {
		super(entityId);
		this.aggregateBehavior = requireNonNull(aggregateBehavior, "aggregateBehavior must be non-null");	    
		this.reactingCommandHandlers = ReactingCommandHandlers.from(aggregateBehavior);
		this.reactingEventHandlers = ReactingEventHandlers.of(aggregateBehavior);

	    createAggregate(entityId, aggregateBehavior);
		
		registerEvents();
	}

	@SuppressWarnings("unchecked")
	private void registerEvents() {
		aggregateBehavior.eventHandlers().getSteps().stream()
			.map(Step::getMessageClass)
			.map(clazz -> (Class<? extends IdentifiedDomainEvent>)clazz)
			.forEach(clazz -> registerEventHandlerFor(clazz));		
	}

	private void registerEventHandlerFor(Class<? extends IdentifiedDomainEvent> eventClass) {
		EventSourced.registerConsumer(EventSourcedAggregateBehavior.class, eventClass, (b, ev) -> {
			reactingEventHandlers.reactTo(ev);
		});
	}

	private void createAggregate(String entityId, AggregateBehavior<STATE> aggregateBehavior) {
		STATE state = aggregateBehavior.initialState(entityId);
	    aggregateBehavior.setState(state);
	}

	public Completes<STATE> reactTo(Object message) {
		Optional<Object> optionalEvent = reactingCommandHandlers.reactTo(message);

		IdentifiedDomainEvent event = optionalEvent.map(ev -> (IdentifiedDomainEvent) ev)
				.orElseThrow(() -> new RuntimeException("Command handler didn't create event!"));

		return apply(event, () -> aggregateBehavior.state());
	}
}