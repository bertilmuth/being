package org.requirementsascode.being;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.lattice.model.DomainEvent;
import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;
import io.vlingo.xoom.lattice.model.sourcing.EventSourced;
import io.vlingo.xoom.symbio.Source;

public class EventSourcedAggregateBehavior<STATE> extends EventSourced implements CompletableBehavior<STATE> {
	private final AggregateBehavior<STATE> aggregateBehavior;
	private final CommandHandlers reactingCommandHandlers;
	private final EventHandlers<STATE> reactingEventHandlers;

	public EventSourcedAggregateBehavior(String entityId, AggregateBehavior<STATE> aggregateBehavior) {
		super(entityId);
		this.aggregateBehavior = requireNonNull(aggregateBehavior, "aggregateBehavior must be non-null");	    
		this.reactingCommandHandlers = aggregateBehavior.commandHandlers();
		this.reactingEventHandlers = aggregateBehavior.eventHandlers();

	    createAggregate(entityId, aggregateBehavior);
		
		registerEventHandlers();
	}

	private void registerEventHandlers() {
		aggregateBehavior.eventHandlers().getEventClasses().stream()
			.forEach(clazz -> registerEventHandlerFor(clazz));		
	}

	private void registerEventHandlerFor(Class<? extends IdentifiedDomainEvent> eventClass) {
		EventSourced.registerConsumer(EventSourcedAggregateBehavior.class, eventClass, (b, ev) -> {
			Optional<STATE> updatedState = reactingEventHandlers.reactTo(ev);
			aggregateBehavior.setState(updatedState.get());
		});
	}

	private void createAggregate(String entityId, AggregateBehavior<STATE> aggregateBehavior) {
		STATE state = aggregateBehavior.initialState(entityId);
	    aggregateBehavior.setState(state);
	}

	public Completes<STATE> reactTo(Object command){
		List<Source<DomainEvent>> events = reactingCommandHandlers.reactTo(command);
		if(events.isEmpty()) {
			throw new RuntimeException("Command handler didn't create event for command: " + command);
		}

		return apply(events, new StateSupplier());
	}
	
	private class StateSupplier implements Supplier<STATE>{
		@Override
		public STATE get() {
			return aggregateBehavior.state();
		}
	}
}