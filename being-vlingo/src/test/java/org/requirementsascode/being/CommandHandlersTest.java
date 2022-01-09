package org.requirementsascode.being;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.requirementsascode.being.CommandHandler.commandsOf;

import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;

class CommandHandlersTest {
	@Test
	void createsEmptyCommandMappers() {
		CommandHandlers<TestCommand> mapCommands = CommandHandlers.with();
		
		List<Class<? extends TestCommand>> commands = mapCommands.getCommandClasses();
		assertTrue(commands.isEmpty());
	}

	@Test
	void createsOneCommandMapper() {
		final Function<SampleCommand1, SampleEvent1> handler = command -> new SampleEvent1(command.id);
		CommandHandlers<TestCommand> mapCommands = CommandHandlers.with(
			commandsOf(SampleCommand1.class).toEvent(handler)
		);
		
		List<Class<? extends TestCommand>> commandClasses = mapCommands.getCommandClasses();
		assertEquals(1, commandClasses.size());
		assertEquals(SampleCommand1.class, commandClasses.get(0));
	}
	
	@Test
	void createsTwoCommandMappers() {
		final Function<SampleCommand1, SampleEvent1> handler1 = command -> new SampleEvent1(command.id);
		CommandHandler<SampleCommand1> mapCommand1 = commandsOf(SampleCommand1.class).toEvent(handler1);
		final Function<SampleCommand2, SampleEvent2> handler2 = command -> new SampleEvent2(command.id);
		CommandHandler<SampleCommand2> mapCommand2 = commandsOf(SampleCommand2.class).toEvent(handler2);

		CommandHandlers<TestCommand> mapCommands = CommandHandlers.with(mapCommand1, mapCommand2);
		List<Class<? extends TestCommand>> commandClasses = mapCommands.getCommandClasses();
		assertEquals(2, commandClasses.size());
		assertEquals(SampleCommand1.class, commandClasses.get(0));
		assertEquals(SampleCommand2.class, commandClasses.get(1));
	}
	
	private interface TestCommand{}
	
	private class SampleCommand1 implements TestCommand{
		public String id;
	};
	
	private class SampleCommand2 implements TestCommand{
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
