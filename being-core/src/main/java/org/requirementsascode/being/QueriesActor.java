package org.requirementsascode.being;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;

import io.vlingo.xoom.actors.ActorInstantiator;
import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.lattice.query.StateStoreQueryActor;
import io.vlingo.xoom.symbio.store.state.StateStore;

@SuppressWarnings("rawtypes")
public class QueriesActor<DATA> extends StateStoreQueryActor implements Queries {
	private final Class<DATA> dataType;
	private final DATA emptyData;

	public QueriesActor(final StateStore store, final Class<DATA> dataType, final DATA emptyData) {
		super(store);
		this.dataType = requireNonNull(dataType, "dataType must be non-null!");
		this.emptyData = requireNonNull(emptyData, "emptyData must be non-null!");
	}

	@Override
	public Completes<DATA> findById(final String id) {
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
	
	@SuppressWarnings("serial")
	public static class Instantiator<DATA> implements ActorInstantiator<QueriesActor>{
		private final StateStore store;
		private final Class<DATA> dataType;
		private final DATA emptyData;

		public Instantiator(final StateStore store, final Class<DATA> dataType, final DATA emptyData) {
			this.store = store;
			this.dataType = dataType;
			this.emptyData = emptyData;
		}

		@Override
		public QueriesActor instantiate() {
			return new QueriesActor<DATA>(store, dataType, emptyData);
		}
		
		@Override
		public Class<QueriesActor> type() {
			return QueriesActor.class;
		}
	}
}
