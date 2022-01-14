package org.requirementsascode.being;


import io.vlingo.xoom.actors.Stage;
import io.vlingo.xoom.lattice.model.sourcing.SourcedTypeRegistry;
import io.vlingo.xoom.symbio.store.dispatch.Dispatcher;

public class Beings {
	@SuppressWarnings("rawtypes")
	public static void baseOn(Stage stage, QueryModel<?> queryModel) {
		final Stage defaultStage = stage.world().stage();
		QueryModelStateStoreProvider.using(defaultStage, queryModel);

		final Dispatcher dispatcher = ProjectionDispatcherProvider.using(defaultStage, queryModel).storeDispatcher;
		final SourcedTypeRegistry sourcedTypeRegistry = new SourcedTypeRegistry(stage.world());
		CommandModelJournalProvider.using(defaultStage, sourcedTypeRegistry, dispatcher);
	}
}
