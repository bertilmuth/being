package org.requirementsascode.being;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.requirementsascode.being.MapCommand.commandsOf;

import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;

class MapCommandsTest {
	@Test
	void createsEmptyCommandMappers() {
		MapCommands mapCommands = MapCommands.with();
		
		List<Class<?>> commands = mapCommands.getCommandClasses();
		assertTrue(commands.isEmpty());
	}

	@Test
	void createsOneCommandMapper() {
		final Function<SampleCommand1, SampleEvent1> handler = command -> new SampleEvent1(command.id);
		MapCommands mapCommands = MapCommands.with(
			commandsOf(SampleCommand1.class).toEvent(handler)
		);
		
		List<Class<?>> commandClasses = mapCommands.getCommandClasses();
		assertEquals(1, commandClasses.size());
		assertEquals(SampleCommand1.class, commandClasses.get(0));
	}
	
	@Test
	void createsTwoCommandMappers() {
		final Function<SampleCommand1, SampleEvent1> handler1 = command -> new SampleEvent1(command.id);
		MapCommand<SampleCommand1> mapCommand1 = commandsOf(SampleCommand1.class).toEvent(handler1);
		final Function<SampleCommand2, SampleEvent2> handler2 = command -> new SampleEvent2(command.id);
		MapCommand<SampleCommand2> mapCommand2 = commandsOf(SampleCommand2.class).toEvent(handler2);

		MapCommands mapCommands = MapCommands.with(mapCommand1, mapCommand2);
		List<Class<?>> commandClasses = mapCommands.getCommandClasses();
		assertEquals(2, commandClasses.size());
		assertEquals(SampleCommand1.class, commandClasses.get(0));
		assertEquals(SampleCommand2.class, commandClasses.get(1));
	}
	
	private class SampleCommand1{
		public String id;
	};
	
	private class SampleCommand2{
		public String id;
	};

	private class SampleEvent1 extends IdentifiedDomainEvent{
		public String id;

		public SampleEvent1(String id) {
			this.id = id;
		}

		@Override
		public String identity() {
			return id;
		}
	};
	
	private class SampleEvent2 extends IdentifiedDomainEvent{
		public String id;
		
		public SampleEvent2(String id) {
			this.id = id;
		}

		@Override
		public String identity() {
			return id;
		}
	};
}
