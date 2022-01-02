package org.requirementsascode.being;

import java.util.Objects;
import java.util.function.Function;

import io.vlingo.xoom.symbio.Source;

public class CommandHandler {
	private final Class<?> commandClass;
	private final Function<?, ? extends Source<?>> handler;

	public static <EVENT extends Source<?>, STATE> CommandHandler commandHandler(Class<?> commandClass, Function<?, ? extends Source<?>> handler) {
		return new CommandHandler(commandClass, handler);
	}
	
	private CommandHandler(Class<?> commandClass, Function<?, ? extends Source<?>> handler) {
		this.commandClass = Objects.requireNonNull(commandClass, "commandClass must be non-null!");
		this.handler = Objects.requireNonNull(handler, "handler must be non-null!");
	}

	public Class<?> getCommandClass() {
		return commandClass;
	}

	public Function<?, ? extends Source<?>> getHandler() {
		return handler;
	}
}
