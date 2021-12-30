package org.requirementsascode.being;

import java.util.ArrayList;
import java.util.Collection;

import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.lattice.query.StateStoreQueryActor;
import io.vlingo.xoom.symbio.store.state.StateStore;

/**
 * See <a href=
 * "https://docs.vlingo.io/xoom-lattice/entity-cqrs#querying-a-statestore">Querying
 * a StateStore</a>
 */
@SuppressWarnings("all")
public class QueriesActor<T> extends StateStoreQueryActor implements Queries {
	private final Class<T> dataType;
	private final T emptyData;

	public QueriesActor(StateStore store, Class<T> dataType, T emptyData) {
		super(store);
		this.dataType = dataType;
		this.emptyData = emptyData;
	}

	@Override
	public Completes<T> findById(String id) {
		return queryStateFor(id, dataType, emptyData);
	}

	@Override
	public Completes<Collection<T>> findAll() {
		return streamAllOf(dataType, new ArrayList<>());
	}

}
