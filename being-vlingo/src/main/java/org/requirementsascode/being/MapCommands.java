package org.requirementsascode.being;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;

public class MapCommands<CMD> implements Function<CMD, List<? extends IdentifiedDomainEvent>>{
	private final List<MapCommand<? extends CMD>> mapCommand;

	@SafeVarargs
	public static <CMD> MapCommands<CMD> with(MapCommand<? extends CMD>... mapCommand) {
		return new MapCommands<>(mapCommand);
	}
	
	@SafeVarargs
	private MapCommands(MapCommand<? extends CMD>... mapCommand) {
		Objects.requireNonNull(mapCommand, "mapCommand must be non-null!");
		this.mapCommand = Arrays.asList(mapCommand);
	}
	
	@Override
	public List<? extends IdentifiedDomainEvent> apply(Object command) {
		Class<?> commandClass = Objects.requireNonNull(command, "command must be non-null!").getClass();
		
		List<? extends IdentifiedDomainEvent> eventList = mapCommand.stream()
			.filter(mapCommand -> mapCommand.getCommandClass().equals(commandClass))
			.findFirst()
			.map(mapCommand -> mapCommand.map(command))
			.orElse(Collections.emptyList());
		
		return eventList;
	}

	public List<Class<? extends CMD>> getCommandClasses() {
		final List<Class<? extends CMD>> commandClasses = 
			mapCommand.stream()
			.map(MapCommand::getCommandClass)
			.collect(Collectors.toList());
		return commandClasses;
	}
}
