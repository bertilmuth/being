package org.requirementsascode.being;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.lattice.query.StateStoreQueryActor;
import io.vlingo.xoom.symbio.store.state.StateStore;

@SuppressWarnings("rawtypes")
public class QueriesActor<DATA> extends StateStoreQueryActor implements Queries {
	private final Class<DATA> dataType;
	private final DATA emptyData;

	public QueriesActor(StateStore store, Class<DATA> dataType, DATA emptyData) {
		super(store);
		this.dataType = Objects.requireNonNull(dataType, "dataType must be non-null!");
		this.emptyData = Objects.requireNonNull(emptyData, "emptyData must be non-null!");
	}

	@Override
	public Completes<DATA> findById(String id) {
		return queryStateFor(id, dataType(), emptyData());
	}

	@Override
	public Completes<Collection<DATA>> findAll() {
		return streamAllOf(dataType(), new ArrayList<>());
	}

	private Class<DATA> dataType() {
		return dataType;
	}

	private DATA emptyData() {
		return emptyData;
	}
}
