package org.requirementsascode.being;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;

public class MapCommands implements Function<Object, List<? extends IdentifiedDomainEvent>>{
	private final List<MapCommand<?>> commandMappers;

	public static MapCommands with(MapCommand<?>... commandMappers) {
		return new MapCommands(commandMappers);
	}
	
	private MapCommands(MapCommand<?>... commandMappers) {
		Objects.requireNonNull(commandMappers, "commandMappers must be non-null!");
		this.commandMappers = Arrays.asList(commandMappers);
	}
	
	@Override
	public List<? extends IdentifiedDomainEvent> apply(Object command) {
		Class<?> commandClass = Objects.requireNonNull(command, "command must be non-null!").getClass();
		
		List<? extends IdentifiedDomainEvent> eventList = commandMappers.stream()
			.filter(h -> h.getCommandClass().equals(commandClass))
			.findFirst()
			.map(h -> h.apply(command))
			.orElse(Collections.emptyList());
		
		return eventList;
	}

	public List<Class<?>> getCommandClasses() {
		final List<Class<?>> commandClasses = 
			commandMappers.stream()
			.map(MapCommand::getCommandClass)
			.collect(Collectors.toList());
		return commandClasses;
	}
}
