package org.requirementsascode.being;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;

public class CommandHandler<CMD>{
	private final Class<CMD> commandClass;
	private final Function<CMD, List<? extends IdentifiedDomainEvent>> mapFunction;

	public static <T> CommandsOf<T> commandsOf(Class<T> commandClass) {
		return new CommandsOf<T>(commandClass);
	}
	
	public static class CommandsOf<T> {
		private final Class<T> commandClass;

		private CommandsOf(Class<T> commandClass) {
			this.commandClass = commandClass;
		}

		public CommandHandler<T> toEvent(Function<T, ? extends IdentifiedDomainEvent> mapFunction) {
			Function<T, List<? extends IdentifiedDomainEvent>> eventListProducingHandler = cmd -> {
				IdentifiedDomainEvent result = mapFunction.apply(cmd);
				return Collections.singletonList(result);
			};
			
			return toEvents(eventListProducingHandler);
		}

		public CommandHandler<T> toEvents(Function<T, List<? extends IdentifiedDomainEvent>> mapFunction) {
			return new CommandHandler<>(commandClass, mapFunction);
		}
	}
	
	private CommandHandler(Class<CMD> commandClass, Function<CMD, List<? extends IdentifiedDomainEvent>> mapFunction) {
		this.commandClass = Objects.requireNonNull(commandClass, "commandClass must be non-null!");
		this.mapFunction = Objects.requireNonNull(mapFunction, "mapFunction must be non-null!");
	}
	
	@SuppressWarnings("unchecked")
	List<? extends IdentifiedDomainEvent> reactTo(Object command) {
		return mapFunction.apply((CMD)command);
	}
	
	public Class<CMD> getCommandClass() {
		return commandClass;
	}
}
