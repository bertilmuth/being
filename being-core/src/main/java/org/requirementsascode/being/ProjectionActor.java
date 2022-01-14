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
		
		logger().info("-----------------------------------------------");
		logger().info("Started merging.");
		logger().info("previousData="+previousData);
		logger().info("previousVersion="+previousVersion);
		logger().info("currentData="+currentData);
		logger().info("currentVersion="+currentVersion);
		logger().info("-----------------------------------------------");
		
		if (previousVersion == currentVersion) {
			logger().info("-----------------------------------------------");
			logger().info("dataToMerge=" + currentData);
			logger().info("-----------------------------------------------");
			return currentData;
		}

		DATA dataToMerge = previousData;

		for (final Source<?> event : sources()) {
			logger().info("Merging data:" + dataToMerge);
			DATA mergedData = queryModel.mergeDataWithEvent(event, dataToMerge);
			logger().info("Merged data:" + mergedData);
			
			if(noMergeHappended(dataToMerge, mergedData)) {
				logger().warn("Event of type " + event.typeName() + " was not matched.");
			} else {
				dataToMerge = mergedData;
			}
		}
		logger().info("-----------------------------------------------");
		logger().info("dataToMerge=" + dataToMerge);
		logger().info("-----------------------------------------------");
		return dataToMerge;
	}

	private boolean noMergeHappended(DATA dataToMerge, DATA mergedData) {
		return dataToMerge == mergedData;
	}
}
