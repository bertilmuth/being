package org.requirementsascode.being.greetingdata;

import static org.requirementsascode.being.CommandHandler.commandsOf;
import static org.requirementsascode.being.EventHandler.eventsOf;

import org.requirementsascode.being.AggregateBehavior;
import org.requirementsascode.being.CommandHandlers;
import org.requirementsascode.being.EventHandlers;

public class Greeting implements AggregateBehavior<GreetingCommand, GreetingState> {
	@Override
	public GreetingState initialState(final String id) {
		return GreetingState.identifiedBy(id);
	}

	@Override
	public CommandHandlers<GreetingCommand, GreetingState> commandHandlers() {
		return CommandHandlers.handle(
			commandsOf(CreateGreeting.class).with((cmd, state) -> new GreetingCreated(state.id, "Hello,", cmd.personName)),
			commandsOf(ChangeSalutation.class).with((cmd, state) -> new SalutationChanged(state.id, cmd.salutation)));
	}

	@Override
	public EventHandlers<GreetingState> eventHandlers() {
		return EventHandlers.handle(
			eventsOf(GreetingCreated.class)
				.with((event, state) -> new GreetingState(state.id, event.salutation, event.personName)),
			eventsOf(SalutationChanged.class)
				.with((event, state) -> new GreetingState(state.id, event.salutation, state.personName)));
	}
}
