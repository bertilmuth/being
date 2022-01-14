package org.requirementsascode.being;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
class QueryModelStateStoreProvider{
	public final StateStore store;
	public final Map<Class<?>, Queries<?>> queriesByDataTypeMap;

	public static QueryModelStateStoreProvider using(final Stage stage, final QueryModel<?>... queryModels) {
		if (ComponentRegistry.has(QueryModelStateStoreProvider.class)) {
			return ComponentRegistry.withType(QueryModelStateStoreProvider.class);
		}

		new EntryAdapterProvider(stage.world()); // future use

		for (QueryModel<?> queryModel : queryModels) {
			registerStoreNameFor(queryModel);
		}

		final StateStore store = StoreActorBuilder.from(stage, Model.QUERY, Arrays.asList(new NoOpDispatcher()),
				StorageType.STATE_STORE, Settings.properties(), true);

		return new QueryModelStateStoreProvider(stage, store, queryModels);
	}

	private QueryModelStateStoreProvider(final Stage stage, final StateStore store, QueryModel<?>[] queryModels) {
		this.store = Objects.requireNonNull(store, "store must be non-null!");	
		Objects.requireNonNull(queryModels, "queryModels must be non-null!");		
		this.queriesByDataTypeMap = new HashMap<>();
		
		for (QueryModel<?> queryModel : queryModels) {
			mapDataTypeToQueries(stage, store, queryModel);
		}
		
		ComponentRegistry.register(getClass(), this);
	}

	private void mapDataTypeToQueries(final Stage stage, final StateStore store, QueryModel<?> queryModel) {
		final Class<?> datatype = datatypeOf(queryModel);
		Queries<?> queriesActor = stage.actorFor(Queries.class, QueriesActor.class, store, datatype, queryModel.emptyData());
		queriesByDataTypeMap.put(datatype, queriesActor);
	}
	
	private static Class<?> registerStoreNameFor(QueryModel<?> queryModel) {
		final Class<?> dataType = datatypeOf(queryModel);
		StateTypeStateStoreMap.stateTypeToStoreName(dataType, dataType.getSimpleName());
		return dataType;
	}

	private static Class<?> datatypeOf(QueryModel<?> queryModel) {
		Object emptyData = queryModel.emptyData();
		final Class<?> dataType = emptyData.getClass();
		return dataType;
	}
}
