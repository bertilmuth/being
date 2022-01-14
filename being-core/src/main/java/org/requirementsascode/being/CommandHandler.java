package org.requirementsascode.being;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;

public class CommandHandler<CMD,STATE>{
	private final Class<CMD> commandClass;
	private final BiFunction<CMD, STATE, List<? extends IdentifiedDomainEvent>> commandHandler;

	public static <T> CommandsOf<T> commandsOf(final Class<T> commandClass) {
		return new CommandsOf<T>(commandClass);
	}
	
	@SuppressWarnings("unchecked")
	List<? extends IdentifiedDomainEvent> reactTo(STATE state, Object command) {
		return commandHandler().apply((CMD)command,state);
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

		public <STATE> CommandHandler<CMD, STATE> with(final BiFunction<CMD, STATE, ? extends IdentifiedDomainEvent> commandHandler) {			
			final BiFunction<CMD, STATE, List<? extends IdentifiedDomainEvent>> eventListProducingHandler = (cmd, state) -> {
				IdentifiedDomainEvent result = commandHandler.apply(cmd, state);
				return Collections.singletonList(result);
			};
			
			return withSome(eventListProducingHandler);
		}

		public <STATE> CommandHandler<CMD, STATE> withSome(final BiFunction<CMD, STATE, List<? extends IdentifiedDomainEvent>> commandHandler) {
			return new CommandHandler<>(commandClass, commandHandler);
		}
	}
	
	private CommandHandler(final Class<CMD> commandClass, final BiFunction<CMD, STATE, List<? extends IdentifiedDomainEvent>> commandHandler) {
		this.commandClass = Objects.requireNonNull(commandClass, "commandClass must be non-null!");
		this.commandHandler = Objects.requireNonNull(commandHandler, "commandHandler must be non-null!");
	}
	
}
