package org.requirementsascode.being;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import io.vlingo.xoom.lattice.model.DomainEvent;
import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;
import io.vlingo.xoom.symbio.Source;

public class CommandHandler<T> {
	private final Class<T> commandClass;
	private final Function<T, List<Source<DomainEvent>>> handler;

	public static <T> CommandsOf<T> commandsOf(Class<T> commandClass) {
		return new CommandsOf<T>(commandClass);
	}

	public static class CommandsOf<T> {
		private final Class<T> commandClass;

		private CommandsOf(Class<T> commandClass) {
			this.commandClass = commandClass;
		}

		CommandHandler<T> toEvent(Function<T, ? extends IdentifiedDomainEvent> handler) {
			Function<T, List<Source<DomainEvent>>> eventListProducingHandler = cmd -> {
				Source<DomainEvent> result = handler.apply(cmd);
				return Collections.singletonList(result);
			};
			
			return toEvents(eventListProducingHandler);
		}

		public CommandHandler<T> toEvents(Function<T, List<Source<DomainEvent>>> handler) {
			return new CommandHandler<>(commandClass, handler);
		}
	}

	private CommandHandler(Class<T> commandClass, Function<T, List<Source<DomainEvent>>> handler) {
		this.commandClass = Objects.requireNonNull(commandClass, "commandClass must be non-null!");
		this.handler = Objects.requireNonNull(handler, "handler must be non-null!");
	}

	public Class<T> getCommandClass() {
		return commandClass;
	}

	public Function<T, List<Source<DomainEvent>>> getHandler() {
		return handler;
	}

	@SuppressWarnings("unchecked")
	List<Source<DomainEvent>> reactTo(Object command) {
		return handler.apply((T) command);
	}
}
