package org.requirementsascode.being;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;

/**
 * Create an instance of this class to define a handler for a specific command type.
 * A command handler transforms an input command into one or several events.
 * 
 * @author b_muth
 *
 * @param <CMD> the specific command type handled by this handler
 * @param <STATE> the type of the state passed in as input to the handler
 */
public class CommandHandler<CMD, STATE> {
	private final Class<CMD> commandClass;
	private final BiFunction<CMD, STATE, List<? extends IdentifiedDomainEvent>> commandHandler;

	/**
	 * Define the specific command type handled by this handler.
	 * 
	 * @param <T> the specific command type handled by this handler
	 * @param commandClass the class of the command type handled by this handler
	 * @return a builder for the command handler
	 */
	public static <T> CommandsOf<T> commandsOf(final Class<T> commandClass) {
		return new CommandsOf<T>(commandClass);
	}

	/**
	 * Reacts to the specified command by transforming the command and state into events.
	 * 
	 * @param command the command to handle
	 * @param state the state used as input to the command handler
	 * @return the list of events produces by the handler
	 */
	@SuppressWarnings("unchecked")
	List<? extends IdentifiedDomainEvent> reactTo(final STATE state, final Object command) {
		return commandHandler().apply((CMD) command, state);
	}

	Class<CMD> commandClass() {
		return commandClass;
	}

	BiFunction<CMD, STATE, List<? extends IdentifiedDomainEvent>> commandHandler() {
		return commandHandler;
	}

	public static class CommandsOf<CMD> {
		private final Class<CMD> commandClass;

		private CommandsOf(Class<CMD> commandClass) {
			this.commandClass = commandClass;
		}

		/**
		 * Define a command handling function that consumes the command and state, and produces a single event.
		 * 
		 * @param <STATE> the type of the state passed in as input to the handler
		 * @param commandHandler the command handling function
		 * @return the command handler instance
		 */
		public <STATE> CommandHandler<CMD, STATE> with(
				final BiFunction<CMD, STATE, ? extends IdentifiedDomainEvent> commandHandler) {
			
			BiFunction<CMD, STATE, List<? extends IdentifiedDomainEvent>> eventListProducingHandler = (cmd, state) -> {
				IdentifiedDomainEvent result = commandHandler.apply(cmd, state);
				return Collections.singletonList(result);
			};

			return withSome(eventListProducingHandler);
		}

		/**
		 * Define a command handling function that consumes the command and state, and produces a list of events.
		 * 
		 * @param <STATE> the type of the state passed in as input to the handler
		 * @param commandHandler the command handling function
		 * @return the command handler instance
		 */
		public <STATE> CommandHandler<CMD, STATE> withSome(
				final BiFunction<CMD, STATE, List<? extends IdentifiedDomainEvent>> commandHandler) {
			return new CommandHandler<>(commandClass, commandHandler);
		}
	}

	private CommandHandler(final Class<CMD> commandClass,
			final BiFunction<CMD, STATE, List<? extends IdentifiedDomainEvent>> commandHandler) {
		this.commandClass = requireNonNull(commandClass, "commandClass must be non-null!");
		this.commandHandler = requireNonNull(commandHandler, "commandHandler must be non-null!");
	}
}
