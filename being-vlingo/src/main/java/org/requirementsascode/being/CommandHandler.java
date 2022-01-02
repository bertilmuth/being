package org.requirementsascode.being;

import java.util.Objects;
import java.util.function.Function;

import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;

public class CommandHandler<T> {
	private final Class<T> commandClass;
	private final Function<T, ? extends IdentifiedDomainEvent> handler;

	public static <T> CommandHandler<T> commandHandler(Class<T> commandClass, Function<T, ? extends IdentifiedDomainEvent> handler) {
		return new CommandHandler<T>(commandClass, handler);
	}
	
	private CommandHandler(Class<T> commandClass, Function<T, ? extends IdentifiedDomainEvent> handler) {
		this.commandClass = Objects.requireNonNull(commandClass, "commandClass must be non-null!");
		this.handler = Objects.requireNonNull(handler, "handler must be non-null!");
	}

	public Class<T> getCommandClass() {
		return commandClass;
	}

	public Function<T, ? extends IdentifiedDomainEvent> getHandler() {
		return handler;
	}
	
	@SuppressWarnings("unchecked")
	IdentifiedDomainEvent reactTo(Object command) {
		return handler.apply((T)command);
	}
}
