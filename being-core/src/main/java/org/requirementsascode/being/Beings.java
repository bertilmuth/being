package org.requirementsascode.being;


import io.vlingo.xoom.actors.Stage;
import io.vlingo.xoom.lattice.model.sourcing.SourcedTypeRegistry;
import io.vlingo.xoom.symbio.store.dispatch.Dispatcher;

/**
 * Class for setting up and managing the infrastructure that the Being library is based on.
 * 
 * @author b_muth
 *
 */
public class Beings {
	
	/**
	 * Sets up and starts up persistence for the aggregate behavior model and query model. 
	 * The events are persisted in a journal, and the query model is persisted using a state store.
	 * After calling this method, the aggregate behavior model and query model are available as actors.
	 * 
	 * @param stage the stage where the actors for the command model and query model are created in
	 * @param queryModel the query model that needs to be connected to the command handling model
	 */
	@SuppressWarnings("rawtypes")
	public static void baseOn(Stage stage, QueryModel<?> queryModel) {
		final Stage defaultStage = stage.world().stage();
		QueryModelStateStoreProvider.using(defaultStage, queryModel);

		final Dispatcher dispatcher = ProjectionDispatcherProvider.using(defaultStage, queryModel).storeDispatcher;
		final SourcedTypeRegistry sourcedTypeRegistry = new SourcedTypeRegistry(stage.world());
		CommandModelJournalProvider.using(defaultStage, sourcedTypeRegistry, dispatcher);
	}
}
