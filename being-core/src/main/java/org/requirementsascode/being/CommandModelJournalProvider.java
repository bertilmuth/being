package org.requirementsascode.being;

import java.util.Arrays;
import static java.util.Objects.requireNonNull;

import java.util.Objects;

import org.requirementsascode.being.EventSourcedAggregate;

import io.vlingo.xoom.actors.Stage;
import io.vlingo.xoom.lattice.model.sourcing.SourcedTypeRegistry;
import io.vlingo.xoom.lattice.model.sourcing.SourcedTypeRegistry.Info;
import io.vlingo.xoom.symbio.store.dispatch.Dispatcher;
import io.vlingo.xoom.symbio.store.dispatch.NoOpDispatcher;
import io.vlingo.xoom.symbio.store.journal.Journal;
import io.vlingo.xoom.turbo.ComponentRegistry;
import io.vlingo.xoom.turbo.actors.Settings;
import io.vlingo.xoom.turbo.annotation.persistence.Persistence.StorageType;
import io.vlingo.xoom.turbo.storage.Model;
import io.vlingo.xoom.turbo.storage.StoreActorBuilder;

@SuppressWarnings("all")
class CommandModelJournalProvider {

	public final Journal<String> journal;

	public static CommandModelJournalProvider using(final Stage stage, final SourcedTypeRegistry registry) {
		return using(stage, registry, new NoOpDispatcher());
	}

	public static CommandModelJournalProvider using(final Stage stage, final SourcedTypeRegistry registry, final Dispatcher... dispatchers) {
		if (ComponentRegistry.has(CommandModelJournalProvider.class)) {
			return ComponentRegistry.withType(CommandModelJournalProvider.class);
		}

		Journal<String> journal = StoreActorBuilder.from(stage, Model.COMMAND, Arrays.asList(dispatchers),
			StorageType.JOURNAL, Settings.properties(), true);

		registry.register(
			new Info(journal, EventSourcedAggregate.class, EventSourcedAggregate.class.getSimpleName()));

		return new CommandModelJournalProvider(journal);
	}

	private CommandModelJournalProvider(final Journal<String> journal) {
		this.journal = requireNonNull(journal, "journal must be non-null!");
		ComponentRegistry.register(getClass(), this);
	}
}
