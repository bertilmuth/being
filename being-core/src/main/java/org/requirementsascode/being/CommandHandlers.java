package org.requirementsascode.being;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;

/**
 * Create an instance of this class to define which command type is handled
 * by which command handler.
 * 
 * @author b_muth
 *
 * @param <CMD> the base type of all handled command types
 * @param <STATE> the type of the state passed in as input to the handlers
 */
public class CommandHandlers<CMD, STATE> {
	private final List<CommandHandler<? extends CMD, STATE>> commandHandlers;

	/**
	 * Define the command handlers that handle the incoming commands.
	 * 
	 * @param <CMD> the base type of all handled command types
	 * @param <STATE> the type of the state passed in as input to the handlers
	 * @param commandHandlers the command handlers
	 * @return an instance of this class
	 */
	@SafeVarargs
	public static <CMD, STATE> CommandHandlers<CMD, STATE> handle(final CommandHandler<? extends CMD, STATE>... commandHandlers) {
		return new CommandHandlers<>(commandHandlers);
	}

	/**
	 * Reacts to the specified command, i.e. looks if there is a command handler
	 * for the command's type, and if yes, uses it to transform the command and state into events.
	 * If there is more than one handler, this method picks the first one.
	 * 
	 * @param command the command to handle
	 * @param state the state used as input to the command handler
	 * @return the list of events produces by the handler, or the empty list if no handler was found
	 */
	public List<? extends IdentifiedDomainEvent> reactTo(final CMD command, final STATE state) {
		Class<?> commandClass = command.getClass();

		List<? extends IdentifiedDomainEvent> eventList = commandHandlers().stream()
			.filter(commandHandler -> commandHandler.commandClass().equals(commandClass))
			.findFirst()
			.map(commandHandler -> commandHandler.reactTo(state, command))
			.orElse(Collections.emptyList());

		return eventList;
	}

	/**
	 * Returns the classes of commands (i.e. the command types) that this instance handles.
	 * 
	 * @return the command types
	 */
	public List<Class<? extends CMD>> commandClasses() {
		List<Class<? extends CMD>> commandClasses = commandHandlers().stream().map(CommandHandler::commandClass)
			.collect(Collectors.toList());
		return commandClasses;
	}

	List<CommandHandler<? extends CMD, STATE>> commandHandlers() {
		return commandHandlers;
	}

	@SafeVarargs
	private CommandHandlers(CommandHandler<? extends CMD, STATE>... commandHandlers) {
		requireNonNull(commandHandlers, "commandHandlers must be non-null!");
		this.commandHandlers = Arrays.asList(commandHandlers);
	}
}
