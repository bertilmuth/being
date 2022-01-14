package org.requirementsascode.being;

import java.util.function.Function;
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
			this.stage = stage;
		}

		public <CMD,STATE> AggregateBuilder<CMD,STATE> aggregateSupplier(
				Supplier<EventSourcedAggregate<CMD,STATE>> aggregateSupplier) {
			return new AggregateBuilder<>(aggregateSupplier);
		}

		public class AggregateBuilder<CMD,STATE> {
			private final Supplier<EventSourcedAggregate<CMD,STATE>> aggregateSupplier;

			AggregateBuilder(Supplier<EventSourcedAggregate<CMD,STATE>> aggregateSupplier) {
				this.aggregateSupplier = aggregateSupplier;
			}

			public <DATA> RequestHandlersBuilder<DATA> queryDataFromState(Function<STATE, DATA> dataFromState) {
				return new RequestHandlersBuilder<DATA>(dataFromState);
			}

			public class RequestHandlersBuilder<DATA> {
				private final HttpRequestHandlers<CMD,STATE, DATA> httpRequestHandlers;

				public RequestHandlersBuilder(Function<STATE, DATA> dataFromState) {
					this.httpRequestHandlers = new HttpRequestHandlers<>(stage, aggregateSupplier, dataFromState);
				}

				public AggregateBuilder<CMD,STATE>.RequestHandlersBuilder<DATA> createRequest(String url,
						Class<? extends CMD> createRequestClass) {
					httpRequestHandlers.createRequest(url, createRequestClass);
					return this;
				}

				public AggregateBuilder<CMD,STATE>.RequestHandlersBuilder<DATA> updateRequest(String url,
						Class<? extends CMD> updateRequestClass) {
					httpRequestHandlers.updateRequest(url, updateRequestClass);
					return this;
				}

				public AggregateBuilder<CMD,STATE>.RequestHandlersBuilder<DATA> findByIdRequest(String url) {
					httpRequestHandlers.findByIdRequest(url);
					return this;
				}

				public AggregateBuilder<CMD,STATE>.RequestHandlersBuilder<DATA> findAllRequest(String url) {
					httpRequestHandlers.findAllRequest(url);
					return this;
				}

				public HttpRequestHandlers<CMD,STATE, DATA> build() {
					return httpRequestHandlers;
				}
			}
		}
	}
}
