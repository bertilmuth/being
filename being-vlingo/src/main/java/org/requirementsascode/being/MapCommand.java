package org.requirementsascode.being;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;

public class MapCommand<CMD>{
	private final Class<CMD> commandClass;
	private final Function<CMD, List<? extends IdentifiedDomainEvent>> mapFunction;

	public static <T> CommandsOf<T> commandsOf(Class<T> commandClass) {
		return new CommandsOf<T>(commandClass);
	}
	
	@SuppressWarnings("unchecked")
	List<? extends IdentifiedDomainEvent> map(Object command) {
		return mapFunction.apply((CMD) command);
	}
	
	public static class CommandsOf<T> {
		private final Class<T> commandClass;

		private CommandsOf(Class<T> commandClass) {
			this.commandClass = commandClass;
		}

		public MapCommand<T> toEvent(Function<T, ? extends IdentifiedDomainEvent> mapFunction) {
			Function<T, List<? extends IdentifiedDomainEvent>> eventListProducingHandler = cmd -> {
				IdentifiedDomainEvent result = mapFunction.apply(cmd);
				return Collections.singletonList(result);
			};
			
			return toEvents(eventListProducingHandler);
		}

		public MapCommand<T> toEvents(Function<T, List<? extends IdentifiedDomainEvent>> mapFunction) {
			return new MapCommand<>(commandClass, mapFunction);
		}
	}

	private MapCommand(Class<CMD> commandClass, Function<CMD, List<? extends IdentifiedDomainEvent>> mapFunction) {
		this.commandClass = Objects.requireNonNull(commandClass, "commandClass must be non-null!");
		this.mapFunction = Objects.requireNonNull(mapFunction, "mapFunction must be non-null!");
	}

	public Class<CMD> getCommandClass() {
		return commandClass;
	}
}
