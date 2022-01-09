package org.requirementsascode.being;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.requirementsascode.being.CommandHandler.commandsOf;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;

class CommandHandlersTest {
	@Test
	void createsEmptyCommandHandlers() {
		CommandHandlers<TestCommand> commandHandlers = CommandHandlers.handle();

		List<Class<? extends TestCommand>> commands = commandHandlers.getCommandClasses();
		assertTrue(commands.isEmpty());
	}

	@Test
	void createsOneCommandMapper() {
		CommandHandlers<TestCommand> commandHandlers = CommandHandlers.handle(
			commandsOf(SampleCommand1.class).toEvent(command -> new SampleEvent1(command.id))
		);

		List<Class<? extends TestCommand>> commandClasses = commandHandlers.getCommandClasses();
		assertEquals(1, commandClasses.size());
		assertEquals(SampleCommand1.class, commandClasses.get(0));
	}

	@Test
	void createsTwoCommandHandlers() {
		CommandHandlers<TestCommand> commandHandlers = CommandHandlers.handle(
			commandsOf(SampleCommand1.class).toEvent(command -> new SampleEvent1(command.id)),
			commandsOf(SampleCommand2.class).toEvent(command -> new SampleEvent2(command.id))
		);
		
		List<Class<? extends TestCommand>> commandClasses = commandHandlers.getCommandClasses();
		assertEquals(2, commandClasses.size());
		assertEquals(SampleCommand1.class, commandClasses.get(0));
		assertEquals(SampleCommand2.class, commandClasses.get(1));
	}

	private interface TestCommand {
	}

	private class SampleCommand1 implements TestCommand {
		public String id;
	};

	private class SampleCommand2 implements TestCommand {
		public String id;
	};

	private class SampleEvent1 extends IdentifiedDomainEvent {
		public String id;

		public SampleEvent1(String id) {
			this.id = id;
		}

		@Override
		public String identity() {
			return id;
		}
	};

	private class SampleEvent2 extends IdentifiedDomainEvent {
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
