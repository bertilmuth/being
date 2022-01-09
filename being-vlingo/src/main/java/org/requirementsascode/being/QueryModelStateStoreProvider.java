package org.requirementsascode.being;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.vlingo.xoom.actors.Stage;
import io.vlingo.xoom.symbio.EntryAdapterProvider;
import io.vlingo.xoom.symbio.store.dispatch.NoOpDispatcher;
import io.vlingo.xoom.symbio.store.state.StateStore;
import io.vlingo.xoom.symbio.store.state.StateTypeStateStoreMap;
import io.vlingo.xoom.turbo.ComponentRegistry;
import io.vlingo.xoom.turbo.actors.Settings;
import io.vlingo.xoom.turbo.annotation.persistence.Persistence.StorageType;
import io.vlingo.xoom.turbo.storage.Model;
import io.vlingo.xoom.turbo.storage.StoreActorBuilder;

@SuppressWarnings("all")
public class QueryModelStateStoreProvider<DATA> {

	public final StateStore store;
	public final Map<Class<? extends DATA>, Queries<DATA>> queriesByDataType;

	public static <DATA> QueryModelStateStoreProvider using(final Stage stage, QueryModel<DATA>... queryModels) {
		if (ComponentRegistry.has(QueryModelStateStoreProvider.class)) {
			return ComponentRegistry.withType(QueryModelStateStoreProvider.class);
		}

		new EntryAdapterProvider(stage.world()); // future use

		for (QueryModel<DATA> queryModel : queryModels) {
			registerStoreNameFor(queryModel);
		}

		final StateStore store = StoreActorBuilder.from(stage, Model.QUERY, Arrays.asList(new NoOpDispatcher()),
				StorageType.STATE_STORE, Settings.properties(), true);

		return new QueryModelStateStoreProvider(stage, store, queryModels);
	}

	private QueryModelStateStoreProvider(final Stage stage, final StateStore store, QueryModel<DATA>[] queryModels) {
		this.store = store;		
		this.queriesByDataType = new HashMap<>();
		
		for (QueryModel<DATA> queryModel : queryModels) {
			mapDataTypeToQueries(stage, store, queryModel);
		}
		
		ComponentRegistry.register(getClass(), this);
	}

	private void mapDataTypeToQueries(final Stage stage, final StateStore store, QueryModel<DATA> queryModel) {
		final Class<? extends DATA> datatype = datatypeOf(queryModel);
		Queries<DATA> queriesActor = stage.actorFor(Queries.class, QueriesActor.class, store, datatype, queryModel.emptyData());
		queriesByDataType.put(datatype, queriesActor);
	}
	
	private static <DATA> Class<? extends DATA> registerStoreNameFor(QueryModel<DATA> queryModel) {
		final Class<? extends DATA> dataType = datatypeOf(queryModel);
		StateTypeStateStoreMap.stateTypeToStoreName(dataType, dataType.getSimpleName());
		return dataType;
	}

	private static <DATA> Class<? extends DATA> datatypeOf(QueryModel<DATA> queryModel) {
		DATA emptyData = queryModel.emptyData();
		final Class<? extends DATA> dataType = (Class<? extends DATA>) emptyData.getClass();
		return dataType;
	}
}
