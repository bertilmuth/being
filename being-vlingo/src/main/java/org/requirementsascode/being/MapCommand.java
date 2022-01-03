package org.requirementsascode.being;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;

public class MapCommand<T> {
	private final Class<T> commandClass;
	private final Function<T, List<? extends IdentifiedDomainEvent>> handler;

	public static <T> CommandsOf<T> commandsOf(Class<T> commandClass) {
		return new CommandsOf<T>(commandClass);
	}

	public static class CommandsOf<T> {
		private final Class<T> commandClass;

		private CommandsOf(Class<T> commandClass) {
			this.commandClass = commandClass;
		}

		MapCommand<T> toEvent(Function<T, ? extends IdentifiedDomainEvent> handler) {
			Function<T, List<? extends IdentifiedDomainEvent>> eventListProducingHandler = cmd -> {
				IdentifiedDomainEvent result = handler.apply(cmd);
				return Collections.singletonList(result);
			};
			
			return toEvents(eventListProducingHandler);
		}

		public MapCommand<T> toEvents(Function<T, List<? extends IdentifiedDomainEvent>> handler) {
			return new MapCommand<>(commandClass, handler);
		}
	}

	private MapCommand(Class<T> commandClass, Function<T, List<? extends IdentifiedDomainEvent>> handler) {
		this.commandClass = Objects.requireNonNull(commandClass, "commandClass must be non-null!");
		this.handler = Objects.requireNonNull(handler, "handler must be non-null!");
	}

	public Class<T> getCommandClass() {
		return commandClass;
	}

	public Function<T, List<? extends IdentifiedDomainEvent>> getHandler() {
		return handler;
	}

	@SuppressWarnings("unchecked")
	List<? extends IdentifiedDomainEvent> reactTo(Object command) {
		return handler.apply((T) command);
	}
}
