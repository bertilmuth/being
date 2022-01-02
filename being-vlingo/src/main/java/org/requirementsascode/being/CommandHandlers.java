package org.requirementsascode.being;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.vlingo.xoom.symbio.Source;

public class CommandHandlers {
	private final List<CommandHandler> commandHandlers;

	public static CommandHandlers are(CommandHandler... commandHandlers) {
		return new CommandHandlers(commandHandlers);
	}
	
	private CommandHandlers(CommandHandler... commandHandlers) {
		Objects.requireNonNull(commandHandlers, "commandHandlers must be non-null!");
		this.commandHandlers = Arrays.asList(commandHandlers);
	}

	public List<Class<?>> getCommandClasses() {
		final List<Class<?>> commandClasses = 
			commandHandlers.stream()
			.map(CommandHandler::getCommandClass)
			.collect(Collectors.toList());
		return commandClasses;
	}

	public List<Function<?, ? extends Source<?>>> getHandlers() {
		List<Function<?, ? extends Source<?>>> handlers = 
			commandHandlers.stream()
			.map(CommandHandler::getHandler)
			.collect(Collectors.toList());
		return handlers;
	}
}
