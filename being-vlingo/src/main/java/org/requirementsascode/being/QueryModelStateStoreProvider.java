package org.requirementsascode.being;

import java.util.Arrays;

import io.vlingo.xoom.actors.Stage;
import io.vlingo.xoom.symbio.EntryAdapterProvider;
import io.vlingo.xoom.symbio.store.dispatch.Dispatcher;
import io.vlingo.xoom.symbio.store.dispatch.NoOpDispatcher;
import io.vlingo.xoom.symbio.store.state.StateStore;
import io.vlingo.xoom.symbio.store.state.StateTypeStateStoreMap;
import io.vlingo.xoom.turbo.ComponentRegistry;
import io.vlingo.xoom.turbo.actors.Settings;
import io.vlingo.xoom.turbo.annotation.persistence.Persistence.StorageType;
import io.vlingo.xoom.turbo.storage.Model;
import io.vlingo.xoom.turbo.storage.StoreActorBuilder;

@SuppressWarnings("all")
public class QueryModelStateStoreProvider<T> {

  public final StateStore store;
  public final Queries queries;

  public static <T> QueryModelStateStoreProvider using(final Stage stage, T emptyData) {
    return using(stage, emptyData, new NoOpDispatcher());
  }

  public static <T> QueryModelStateStoreProvider using(final Stage stage,  T emptyData, final Dispatcher ...dispatchers) {
    if (ComponentRegistry.has(QueryModelStateStoreProvider.class)) {
      return ComponentRegistry.withType(QueryModelStateStoreProvider.class);
    }

    new EntryAdapterProvider(stage.world()); // future use

    final Class<? extends T> dataType = (Class<? extends T>) emptyData.getClass();
	StateTypeStateStoreMap.stateTypeToStoreName(dataType, dataType.getSimpleName());

    final StateStore store =
            StoreActorBuilder.from(stage, Model.QUERY, Arrays.asList(dispatchers), StorageType.STATE_STORE, Settings.properties(), true);


    return new QueryModelStateStoreProvider(stage, store, dataType, emptyData);
  }

  private QueryModelStateStoreProvider(final Stage stage, final StateStore store, Class<T> dataType, T emptyData) {
    this.store = store;
    this.queries = stage.actorFor(Queries.class, QueriesActor.class, store, dataType, emptyData);
    ComponentRegistry.register(getClass(), this);
  }
}
