package org.requirementsascode.being;

import java.util.Objects;

import io.vlingo.xoom.lattice.model.projection.Projectable;
import io.vlingo.xoom.lattice.model.projection.StateStoreProjectionActor;
import io.vlingo.xoom.symbio.Source;
import io.vlingo.xoom.symbio.store.state.StateStore;
import io.vlingo.xoom.turbo.ComponentRegistry;

/**
 * See <a href=
 * "https://docs.vlingo.io/xoom-lattice/projections#implementing-with-the-statestoreprojectionactor">
 * StateStoreProjectionActor </a>
 * @param <DATA> the data type of the projected view model
 */
public class ProjectionActor<DATA> extends StateStoreProjectionActor<DATA> {
	private final QueryModel<DATA> queryModel;
	private final DATA emptyData;

	public <U> ProjectionActor(QueryModel<DATA> queryModel) {
		this(ComponentRegistry.withType(QueryModelStateStoreProvider.class).store, queryModel);
	}

	public ProjectionActor(StateStore store, QueryModel<DATA> queryModel) {
		super(store);
		this.queryModel = Objects.requireNonNull(queryModel, "queryModel must be non-null!");
		this.emptyData = Objects.requireNonNull(queryModel.emptyData(), "emptyData(...) must return a non-null value!");
	}

	@Override
	protected DATA currentDataFor(final Projectable projectable) {
		return emptyData;
	}

	@Override
	protected DATA merge(final DATA previousData, final int previousVersion,
			final DATA currentData, final int currentVersion) {
		DATA dataToMerge = previousData;

		for (final Source<?> event : sources()) {
			DATA mergedData = queryModel.mergeDataWithEvent(event, dataToMerge);
			
			if(noMergeHappended(dataToMerge, mergedData)) {
				logger().warn("Event of type " + event.typeName() + " was not matched.");
			} else {
				dataToMerge = mergedData;
			}
		}
		return dataToMerge;
	}

	private boolean noMergeHappended(DATA dataToMerge, DATA mergedData) {
		return dataToMerge == mergedData;
	}
}
