package org.requirementsascode.being;

import java.util.Objects;

import io.vlingo.xoom.actors.Logger;
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
	private final Logger logger;

	public <U> ProjectionActor(QueryModel<DATA> queryModel) {
		this(ComponentRegistry.withType(QueryModelStateStoreProvider.class).store, queryModel);
	}

	public ProjectionActor(StateStore store, QueryModel<DATA> queryModel) {
		super(store);
		this.queryModel = Objects.requireNonNull(queryModel, "queryModel must be non-null!");
		this.logger = super.stage().world().defaultLogger();
	}

	@Override
	protected DATA currentDataFor(final Projectable projectable) {
		return queryModel.emptyData();
	}

	@Override
	protected DATA merge(final DATA previousData, final int previousVersion,
			final DATA currentData, final int currentVersion) {

		if (previousVersion == currentVersion)
			return currentData;

		DATA dataToMerge = previousData;
		logger.info("Merging data:" + dataToMerge);

		for (final Source<?> event : sources()) {
			DATA mergedData = queryModel.mergeDataWithEvent(dataToMerge, event);
			logger.info("Merged data:" + mergedData);
			
			if(dataToMerge == mergedData) {
				logger().warn("Event of type " + event.typeName() + " was not matched.");
				break;
			} else {
				dataToMerge = mergedData;
			}
		}

		return dataToMerge;
	}
}
