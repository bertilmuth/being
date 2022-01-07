package org.requirementsascode.being;

import static io.vlingo.xoom.common.serialization.JsonSerialization.serialized;
import static io.vlingo.xoom.http.Response.Status.Created;
import static io.vlingo.xoom.http.Response.Status.InternalServerError;
import static io.vlingo.xoom.http.Response.Status.NotFound;
import static io.vlingo.xoom.http.Response.Status.Ok;
import static io.vlingo.xoom.http.resource.ResourceBuilder.get;
import static io.vlingo.xoom.http.resource.ResourceBuilder.patch;
import static io.vlingo.xoom.http.resource.ResourceBuilder.post;

import java.util.Collection;
import java.util.function.Function;

import io.vlingo.xoom.actors.Address;
import io.vlingo.xoom.actors.Definition;
import io.vlingo.xoom.actors.Stage;
import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.http.Response;
import io.vlingo.xoom.http.resource.RequestHandler0;
import io.vlingo.xoom.http.resource.RequestHandler1;
import io.vlingo.xoom.http.resource.RequestHandler2;
import io.vlingo.xoom.lattice.grid.Grid;

public class HttpRequestHandlersBuilder {
	HttpRequestHandlersBuilder() {
	}

	public GridBuilder grid(Grid grid) {
		return new GridBuilder(grid);
	}

	public class GridBuilder {
		private final Grid grid;

		GridBuilder(Grid grid) {
			this.grid = grid;
		}

		public <STATE> AggregateBuilder<STATE> aggregate(EventSourcedAggregate<STATE> aggregate) {
			return new AggregateBuilder<>(aggregate);
		}

		public class AggregateBuilder<STATE> {
			private final EventSourcedAggregate<STATE> aggregate;

			AggregateBuilder(EventSourcedAggregate<STATE> aggregate) {
				this.aggregate = aggregate;
			}

			public <DATA> DataFromStateBuilder<DATA> dataFromState(Function<STATE, DATA> dataFromState) {
				return new DataFromStateBuilder<DATA>(dataFromState);
			}

			public class DataFromStateBuilder<DATA> {
				private final Function<STATE, DATA> dataFromState;
				private final HttpRequestHandlers<DATA> httpRequestHandlers;

				public DataFromStateBuilder(Function<STATE, DATA> dataFromState) {
					this.dataFromState = dataFromState;
					this.httpRequestHandlers = new HttpRequestHandlers<>(grid, resourceName());
				}

				private String resourceName() {
					return aggregate.getClass().getSimpleName() + "RequestHandlers";
				}

				public AggregateBuilder<STATE>.DataFromStateBuilder<DATA> createRequest(String url,
						Class<?> createRequestClass) {
					final RequestHandler1<?> handler = post(url).body(createRequestClass).handle(this::createAggregate);
					httpRequestHandlers.add(handler);
					return this;
				}

				public AggregateBuilder<STATE>.DataFromStateBuilder<DATA> updateRequest(String url,
						Class<?> updateRequestClass) {
					final RequestHandler2<String, ?> handler = patch(url).param(String.class).body(updateRequestClass)
							.handle(this::updateAggregate);
					httpRequestHandlers.add(handler);
					return this;
				}

				public AggregateBuilder<STATE>.DataFromStateBuilder<DATA> findByIdRequest(String url) {
					final RequestHandler1<String> handler = get(url).param(String.class)
							.handle(this::findAggregateById);
					httpRequestHandlers.add(handler);
					return this;
				}

				public AggregateBuilder<STATE>.DataFromStateBuilder<DATA> findAllRequest(String url) {
					final RequestHandler0 handler = get(url).handle(this::findAllAggregates);
					httpRequestHandlers.add(handler);
					return this;
				}

				public HttpRequestHandlers<DATA> build() {
					return httpRequestHandlers;
				}

				private Completes<Response> createAggregate(Object request) {
					return createAggregateOnStage(grid, request).andThenTo(state -> {
						return Completes
								.withSuccess(httpRequestHandlers.responseOf(Created,
										serialized(dataFromState.apply(state))))
								.otherwise(arg -> Response.of(NotFound))
								.recoverFrom(e -> Response.of(InternalServerError, e.getMessage()));
					});

				}

				private Completes<Response> updateAggregate(final String id, final Object request) {
					return resolve(id).andThenTo(behavior -> reactTo(behavior, request))
							.andThenTo(state -> Completes.withSuccess(
									httpRequestHandlers.responseOf(Ok, serialized(dataFromState.apply(state)))))
							.otherwise(noData -> Response.of(NotFound))
							.recoverFrom(e -> Response.of(InternalServerError, e.getMessage()));
				}

				private Completes<Response> findAllAggregates() {
					Completes<Collection<DATA>> findAll = httpRequestHandlers.queries().findAll();
					return findAll
							.andThenTo(
									data -> Completes.withSuccess(httpRequestHandlers.responseOf(Ok, serialized(data))))
							.otherwise(arg -> Response.of(NotFound))
							.recoverFrom(e -> Response.of(InternalServerError, e.getMessage()));
				}

				private Completes<Response> findAggregateById(final String id) {
					Completes<DATA> findById = httpRequestHandlers.queries().findById(id);
					return findById
							.andThenTo(
									data -> Completes.withSuccess(httpRequestHandlers.responseOf(Ok, serialized(data))))
							.otherwise(arg -> Response.of(NotFound))
							.recoverFrom(e -> Response.of(InternalServerError, e.getMessage()));
				}

				@SuppressWarnings({ "unchecked", "rawtypes" })
				private Completes<STATE> reactTo(Behavior behavior, Object command) {
					return behavior.reactTo(command);
				}

				@SuppressWarnings("rawtypes")
				private Completes<Behavior> resolve(final String id) {
					final Address address = grid.addressFactory().from(id);
					return grid.actorOf(Behavior.class, address,
							Definition.has(EventSourcedBehavior.class, Definition.parameters(id)));
				}

				@SuppressWarnings("unchecked")
				private Completes<STATE> createAggregateOnStage(final Stage stage, final Object command) {
					final io.vlingo.xoom.actors.Address _address = stage.addressFactory().uniquePrefixedWith("g-");
					final Behavior<STATE> behavior = stage.actorFor(Behavior.class, Definition
							.has(EventSourcedBehavior.class, Definition.parameters(_address.idString(), aggregate)),
							_address);
					return reactTo(behavior, command);
				}
			}
		}
	}
}
