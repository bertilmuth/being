package org.requirementsascode.being;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;

public class MapCommands implements Function<Object, List<? extends IdentifiedDomainEvent>>{
	private final List<MapCommand<?>> mapCommands;

	public static MapCommands with(MapCommand<?>... mapCommand) {
		return new MapCommands(mapCommand);
	}
	
	private MapCommands(MapCommand<?>... mapCommands) {
		Objects.requireNonNull(mapCommands, "mapCommands must be non-null!");
		this.mapCommands = Arrays.asList(mapCommands);
	}
	
	public List<? extends IdentifiedDomainEvent> apply(Object command) {
		Class<?> commandClass = Objects.requireNonNull(command, "command must be non-null!").getClass();
		
		List<? extends IdentifiedDomainEvent> eventList = mapCommands.stream()
			.filter(h -> h.getCommandClass().equals(commandClass))
			.findFirst()
			.map(h -> h.reactTo(command))
			.orElse(Collections.emptyList());
		
		return eventList;
	}

	public List<Class<?>> getCommandClasses() {
		final List<Class<?>> commandClasses = 
			mapCommands.stream()
			.map(MapCommand::getCommandClass)
			.collect(Collectors.toList());
		return commandClasses;
	}
}
