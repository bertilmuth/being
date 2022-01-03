package org.requirementsascode.being;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;

public class MapCommand<T> implements Function<Object, List<? extends IdentifiedDomainEvent>>{
	private final Class<T> commandClass;
	private final Function<T, List<? extends IdentifiedDomainEvent>> mapFunction;

	public static <T> CommandsOf<T> commandsOf(Class<T> commandClass) {
		return new CommandsOf<T>(commandClass);
	}

	public static class CommandsOf<T> {
		private final Class<T> commandClass;

		private CommandsOf(Class<T> commandClass) {
			this.commandClass = commandClass;
		}

		MapCommand<T> toEvent(Function<T, ? extends IdentifiedDomainEvent> mapFunction) {
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

	private MapCommand(Class<T> commandClass, Function<T, List<? extends IdentifiedDomainEvent>> mapFunction) {
		this.commandClass = Objects.requireNonNull(commandClass, "commandClass must be non-null!");
		this.mapFunction = Objects.requireNonNull(mapFunction, "mapFunction must be non-null!");
	}

	public Class<T> getCommandClass() {
		return commandClass;
	}

	@SuppressWarnings("unchecked")
	public List<? extends IdentifiedDomainEvent> apply(Object command) {
		return mapFunction.apply((T) command);
	}
}
