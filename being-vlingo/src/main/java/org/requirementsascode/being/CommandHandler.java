package org.requirementsascode.being;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;

public class CommandHandler<CMD>{
	private final Class<CMD> commandClass;
	private final Function<CMD, List<? extends IdentifiedDomainEvent>> commandHandler;

	public static <T> CommandsOf<T> commandsOf(Class<T> commandClass) {
		return new CommandsOf<T>(commandClass);
	}
	
	public static class CommandsOf<T> {
		private final Class<T> commandClass;

		private CommandsOf(Class<T> commandClass) {
			this.commandClass = commandClass;
		}

		public CommandHandler<T> with(Function<T, ? extends IdentifiedDomainEvent> commandHandler) {
			Function<T, List<? extends IdentifiedDomainEvent>> eventListProducingHandler = cmd -> {
				IdentifiedDomainEvent result = commandHandler.apply(cmd);
				return Collections.singletonList(result);
			};
			
			return withSome(eventListProducingHandler);
		}

		public CommandHandler<T> withSome(Function<T, List<? extends IdentifiedDomainEvent>> commandHandler) {
			return new CommandHandler<>(commandClass, commandHandler);
		}
	}
	
	private CommandHandler(Class<CMD> commandClass, Function<CMD, List<? extends IdentifiedDomainEvent>> commandHandler) {
		this.commandClass = Objects.requireNonNull(commandClass, "commandClass must be non-null!");
		this.commandHandler = Objects.requireNonNull(commandHandler, "commandHandler must be non-null!");
	}
	
	@SuppressWarnings("unchecked")
	List<? extends IdentifiedDomainEvent> reactTo(Object command) {
		return commandHandler.apply((CMD)command);
	}
	
	public Class<CMD> getCommandClass() {
		return commandClass;
	}
}
