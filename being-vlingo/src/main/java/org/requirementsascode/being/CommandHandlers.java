package org.requirementsascode.being;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;

public class CommandHandlers<CMD,STATE>{
	private final List<CommandHandler<? extends CMD, STATE>> commandHandlers;
	
	@SafeVarargs
	public static <CMD,STATE> CommandHandlers<CMD,STATE> handle(CommandHandler<? extends CMD, STATE>... commandHandlers) {
		return new CommandHandlers<>(commandHandlers);
	}

	public List<? extends IdentifiedDomainEvent> reactTo(CMD command,STATE state) {
		Class<?> commandClass = Objects.requireNonNull(command, "command must be non-null!").getClass();
		Objects.requireNonNull(state, "state must be non-null!");
		
		List<? extends IdentifiedDomainEvent> eventList = commandHandlers().stream()
			.filter(commandHandler -> commandHandler.commandClass().equals(commandClass))
			.findFirst()
			.map(commandHandler -> commandHandler.reactTo(state, command))
			.orElse(Collections.emptyList());
		
		return eventList;
	}
	
	public List<Class<? extends CMD>> commandClasses() {
		final List<Class<? extends CMD>> commandClasses = 
			commandHandlers().stream()
			.map(CommandHandler::commandClass)
			.collect(Collectors.toList());
		return commandClasses;
	}
	
	List<CommandHandler<? extends CMD, STATE>> commandHandlers(){
		return commandHandlers;
	}
	
	@SafeVarargs
	private CommandHandlers(CommandHandler<? extends CMD, STATE>... commandHandlers) {
		Objects.requireNonNull(commandHandlers, "commandHandlers must be non-null!");
		this.commandHandlers = Arrays.asList(commandHandlers);
	}
}
