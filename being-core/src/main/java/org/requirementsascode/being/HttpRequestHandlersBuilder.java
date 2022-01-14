package org.requirementsascode.being;

import java.util.function.Function;
import static java.util.Objects.*;

import java.util.function.Supplier;

import io.vlingo.xoom.actors.Stage;

public class HttpRequestHandlersBuilder {
	HttpRequestHandlersBuilder() {
	}

	public StageBuilder stage(Stage stage) {
		return new StageBuilder(stage);
	}

	public class StageBuilder {
		private final Stage stage;

		StageBuilder(Stage stage) {
			this.stage = requireNonNull(stage,"stage must be non-null!");
		}

		public <CMD, STATE> AggregateBuilder<CMD, STATE> behaviorSupplier(Supplier<AggregateBehavior<CMD, STATE>> behaviorSupplier) {
			return new AggregateBuilder<>(behaviorSupplier);
		}

		public class AggregateBuilder<CMD, STATE> {
			private final Supplier<AggregateBehavior<CMD, STATE>> behaviorSupplier;

			AggregateBuilder(final Supplier<AggregateBehavior<CMD, STATE>> behaviorSupplier) {
				this.behaviorSupplier = requireNonNull(behaviorSupplier,"behaviorSupplier must be non-null!");
			}

			public <DATA> RequestHandlersBuilder<DATA> queryDataFromState(final Function<STATE, DATA> dataFromState) {
				return new RequestHandlersBuilder<DATA>(dataFromState);
			}

			public class RequestHandlersBuilder<DATA> {
				private final HttpRequestHandlers<CMD, STATE, DATA> httpRequestHandlers;

				public RequestHandlersBuilder(Function<STATE, DATA> dataFromState) {
					this.httpRequestHandlers = new HttpRequestHandlers<>(stage, behaviorSupplier, dataFromState);
				}

				public AggregateBuilder<CMD, STATE>.RequestHandlersBuilder<DATA> createRequest(String url, Class<? extends CMD> createRequestClass) {
					httpRequestHandlers.createRequest(url, createRequestClass);
					return this;
				}

				public AggregateBuilder<CMD, STATE>.RequestHandlersBuilder<DATA> updateRequest(String url, Class<? extends CMD> updateRequestClass) {
					httpRequestHandlers.updateRequest(url, updateRequestClass);
					return this;
				}

				public AggregateBuilder<CMD, STATE>.RequestHandlersBuilder<DATA> findByIdRequest(String url) {
					httpRequestHandlers.findByIdRequest(url);
					return this;
				}

				public AggregateBuilder<CMD, STATE>.RequestHandlersBuilder<DATA> findAllRequest(String url) {
					httpRequestHandlers.findAllRequest(url);
					return this;
				}

				public HttpRequestHandlers<CMD, STATE, DATA> build() {
					return httpRequestHandlers;
				}
			}
		}
	}
}
