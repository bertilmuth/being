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

		public <STATE> AggregateBuilder<STATE> aggregateSupplier(
				Supplier<EventSourcedAggregate<STATE>> aggregateSupplier) {
			return new AggregateBuilder<>(aggregateSupplier);
		}

		public class AggregateBuilder<STATE> {
			private final Supplier<EventSourcedAggregate<STATE>> aggregateSupplier;

			AggregateBuilder(Supplier<EventSourcedAggregate<STATE>> aggregateSupplier) {
				this.aggregateSupplier = aggregateSupplier;
			}

			public <DATA> RequestHandlersBuilder<DATA> dataFromState(Function<STATE, DATA> dataFromState) {
				return new RequestHandlersBuilder<DATA>(dataFromState);
			}

			public class RequestHandlersBuilder<DATA> {
				private final HttpRequestHandlers<STATE, DATA> httpRequestHandlers;
				private final Class<? extends Object> dataTypeOfAggregate;

				public RequestHandlersBuilder(Function<STATE, DATA> dataFromState) {
					final EventSourcedAggregate<STATE> aggregate = aggregateSupplier.get();					
					this.dataTypeOfAggregate = dataTypeOfAggregate(aggregate, dataFromState);
					
					this.httpRequestHandlers = new HttpRequestHandlers<>(stage, resourceNameOf(aggregate), dataTypeOfAggregate, aggregateSupplier, dataFromState);
				}

				private Class<? extends Object> dataTypeOfAggregate(EventSourcedAggregate<STATE> aggregate,
						Function<STATE, DATA> dataFromState) {
					Class<? extends Object> aggregateDataClass = dataFromState.apply(aggregate.initialState(null))
							.getClass();
					return aggregateDataClass;
				}

				private String resourceNameOf(EventSourcedAggregate<STATE> aggregate) {
					String resourceName = aggregate.getClass().getSimpleName() + "RequestHandlers";
					return resourceName;
				}

				public AggregateBuilder<STATE>.RequestHandlersBuilder<DATA> createRequest(String url,
						Class<?> createRequestClass) {
					httpRequestHandlers.createRequest(url, createRequestClass);
					return this;
				}

				public AggregateBuilder<STATE>.RequestHandlersBuilder<DATA> updateRequest(String url,
						Class<?> updateRequestClass) {
					httpRequestHandlers.updateRequest(url, updateRequestClass);
					return this;
				}

				public AggregateBuilder<STATE>.RequestHandlersBuilder<DATA> findByIdRequest(String url) {
					httpRequestHandlers.findByIdRequest(url);
					return this;
				}

				public AggregateBuilder<STATE>.RequestHandlersBuilder<DATA> findAllRequest(String url) {
					httpRequestHandlers.findAllRequest(url);
					return this;
				}

				public HttpRequestHandlers<STATE, DATA> build() {
					return httpRequestHandlers;
				}
			}
		}
	}
}
