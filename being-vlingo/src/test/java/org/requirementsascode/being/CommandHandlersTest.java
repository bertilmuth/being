package org.requirementsascode.being;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.requirementsascode.being.CommandHandler.commandsOf;

import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import io.vlingo.xoom.lattice.model.DomainEvent;
import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;
import io.vlingo.xoom.symbio.Source;

class CommandHandlersTest {
	@Test
	void createsEmptyCommandHandlers() {
		CommandHandlers commandHandlers = CommandHandlers.are();
		
		List<Class<?>> commands = commandHandlers.getCommandClasses();
		assertTrue(commands.isEmpty());
		
		List<Function<?, List<Source<DomainEvent>>>> handlers = commandHandlers.getHandlers();
		assertTrue(handlers.isEmpty());
	}

	@Test
	void createsOneCommandHandler() {
		final Function<SampleCommand1, SampleEvent1> handler = command -> new SampleEvent1(command.id);
		CommandHandlers commandHandlers = CommandHandlers.are(
			commandsOf(SampleCommand1.class).toEvent(handler)
		);
		
		List<Class<?>> commandClasses = commandHandlers.getCommandClasses();
		assertEquals(1, commandClasses.size());
		assertEquals(SampleCommand1.class, commandClasses.get(0));
		
		List<Function<?, List<Source<DomainEvent>>>> handlers = commandHandlers.getHandlers();
		assertEquals(1, handlers.size());
	}
	
	@Test
	void createsTwoCommandHandlers() {
		final Function<SampleCommand1, SampleEvent1> handler1 = command -> new SampleEvent1(command.id);
		CommandHandler<SampleCommand1> commandHandler1 = commandsOf(SampleCommand1.class).toEvent(handler1);
		final Function<SampleCommand2, SampleEvent2> handler2 = command -> new SampleEvent2(command.id);
		CommandHandler<SampleCommand2> commandHandler2 = commandsOf(SampleCommand2.class).toEvent(handler2);

		CommandHandlers commandHandlers = CommandHandlers.are(commandHandler1, commandHandler2);
		List<Class<?>> commandClasses = commandHandlers.getCommandClasses();
		assertEquals(2, commandClasses.size());
		assertEquals(SampleCommand1.class, commandClasses.get(0));
		assertEquals(SampleCommand2.class, commandClasses.get(1));
		
		List<Function<?, List<Source<DomainEvent>>>> handlers = commandHandlers.getHandlers();
		assertEquals(2, handlers.size());
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
