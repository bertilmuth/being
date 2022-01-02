package org.requirementsascode.being;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.requirementsascode.being.CommandHandler.commandHandler;

import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;
import io.vlingo.xoom.symbio.Source;

class CommandHandlersTest {
	@Test
	void createsEmptyCommandHandlers() {
		CommandHandlers commandHandlers = CommandHandlers.are();
		
		List<Class<?>> commands = commandHandlers.getCommandClasses();
		assertTrue(commands.isEmpty());
		
		List<Function<?, ? extends Source<?>>> handlers = commandHandlers.getHandlers();
		assertTrue(handlers.isEmpty());
	}

	@Test
	void createsOneCommandHandler() {
		final Function<SampleCommand1, SampleEvent1> handler = command -> new SampleEvent1(command.id);
		CommandHandlers commandHandlers = CommandHandlers.are(
			commandHandler(SampleCommand1.class, handler)
		);
		
		List<Class<?>> commandClasses = commandHandlers.getCommandClasses();
		assertEquals(1, commandClasses.size());
		assertEquals(SampleCommand1.class, commandClasses.get(0));
		
		List<Function<?, ? extends Source<?>>> handlers = commandHandlers.getHandlers();
		assertEquals(1, handlers.size());
		assertEquals(handler, handlers.get(0));
	}
	
	@Test
	void createsTwoCommandHandlers() {
		final Function<SampleCommand1, SampleEvent1> handler1 = command -> new SampleEvent1(command.id);
		CommandHandler commandHandler1 = commandHandler(SampleCommand1.class, handler1);
		final Function<SampleCommand2, SampleEvent2> handler2 = command -> new SampleEvent2(command.id);
		CommandHandler commandHandler2 = commandHandler(SampleCommand2.class, handler2);

		CommandHandlers commandHandlers = CommandHandlers.are(commandHandler1, commandHandler2);
		List<Class<?>> commandClasses = commandHandlers.getCommandClasses();
		assertEquals(2, commandClasses.size());
		assertEquals(SampleCommand1.class, commandClasses.get(0));
		assertEquals(SampleCommand2.class, commandClasses.get(1));
		
		List<Function<?, ? extends Source<?>>> handlers = commandHandlers.getHandlers();
		assertEquals(2, handlers.size());
		assertEquals(handler1, handlers.get(0));
		assertEquals(handler2, handlers.get(1));
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
