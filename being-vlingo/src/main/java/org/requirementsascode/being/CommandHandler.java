package org.requirementsascode.being;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;

public class CommandHandler<STATE, CMD>{
	private final Class<CMD> commandClass;
	private final BiFunction<STATE, CMD, List<? extends IdentifiedDomainEvent>> commandHandler;

	public static <T> CommandsOf<T> commandsOf(Class<T> commandClass) {
		return new CommandsOf<T>(commandClass);
	}
	
	public static class CommandsOf<CMD> {
		private final Class<CMD> commandClass;

		private CommandsOf(Class<CMD> commandClass) {
			this.commandClass = commandClass;
		}

		public <STATE> CommandHandler<STATE, CMD> with(BiFunction<STATE, CMD, ? extends IdentifiedDomainEvent> commandHandler) {
			BiFunction<STATE, CMD, List<? extends IdentifiedDomainEvent>> eventListProducingHandler = (state,cmd) -> {
				IdentifiedDomainEvent result = commandHandler.apply(state, cmd);
				return Collections.singletonList(result);
			};
			
			return withSome(eventListProducingHandler);
		}

		public <STATE> CommandHandler<STATE, CMD> withSome(BiFunction<STATE, CMD, List<? extends IdentifiedDomainEvent>> commandHandler) {
			return new CommandHandler<>(commandClass, commandHandler);
		}
	}
	
	private CommandHandler(Class<CMD> commandClass, BiFunction<STATE, CMD, List<? extends IdentifiedDomainEvent>> commandHandler) {
		this.commandClass = Objects.requireNonNull(commandClass, "commandClass must be non-null!");
		this.commandHandler = Objects.requireNonNull(commandHandler, "commandHandler must be non-null!");
	}
	
	@SuppressWarnings("unchecked")
	List<? extends IdentifiedDomainEvent> reactTo(STATE state, Object command) {
		return commandHandler.apply(state, (CMD)command);
	}
	
	public Class<CMD> getCommandClass() {
		return commandClass;
	}
}
