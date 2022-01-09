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
import java.util.function.Supplier;

import io.vlingo.xoom.actors.Address;
import io.vlingo.xoom.actors.Definition;
import io.vlingo.xoom.actors.Stage;
import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.http.Response;
import io.vlingo.xoom.http.resource.RequestHandler0;
import io.vlingo.xoom.http.resource.RequestHandler1;
import io.vlingo.xoom.http.resource.RequestHandler2;

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
				private final Function<STATE, DATA> dataFromState;
				private final HttpRequestHandlers<DATA> httpRequestHandlers;
				private final Class<? extends Object> dataTypeOfAggregate;

				public RequestHandlersBuilder(Function<STATE, DATA> dataFromState) {
					this.dataFromState = dataFromState;

					final EventSourcedAggregate<STATE> aggregate = aggregateSupplier.get();
					
					this.dataTypeOfAggregate = dataTypeOfAggregate(aggregate, dataFromState);
					this.httpRequestHandlers = new HttpRequestHandlers<>(stage, resourceNameOf(aggregate), dataTypeOfAggregate);
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
					final RequestHandler1<?> handler = post(url).body(createRequestClass).handle(this::createAggregate);
					httpRequestHandlers.add(handler);
					return this;
				}

				public AggregateBuilder<STATE>.RequestHandlersBuilder<DATA> updateRequest(String url,
						Class<?> updateRequestClass) {
					final RequestHandler2<String, ?> handler = patch(url).param(String.class).body(updateRequestClass)
							.handle(this::updateAggregate);
					httpRequestHandlers.add(handler);
					return this;
				}

				public AggregateBuilder<STATE>.RequestHandlersBuilder<DATA> findByIdRequest(String url) {
					final RequestHandler1<String> handler = get(url).param(String.class)
							.handle(this::findAggregateById);
					httpRequestHandlers.add(handler);
					return this;
				}

				public AggregateBuilder<STATE>.RequestHandlersBuilder<DATA> findAllRequest(String url) {
					final RequestHandler0 handler = get(url).handle(this::findAllAggregates);
					httpRequestHandlers.add(handler);
					return this;
				}

				public HttpRequestHandlers<DATA> build() {
					return httpRequestHandlers;
				}

				private Completes<Response> createAggregate(Object request) {
					return createAggregateOnStage(stage, request).andThenTo(state -> {
						return Completes
								.withSuccess(
										httpRequestHandlers.responseOf(Created, serialized(dataFromState.apply(state))))
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
					final Address address = stage.addressFactory().from(id);
					final Completes<Behavior> actor = stage.actorOf(Behavior.class, address,
							Definition.has(EventSourcedAggregateBehavior.class, Definition.parameters(id)));
					stage.world().defaultLogger().info("Resolved actor: " + actor.id());
					return actor;
				}

				@SuppressWarnings("unchecked")
				private Completes<STATE> createAggregateOnStage(final Stage stage, final Object command) {
					final io.vlingo.xoom.actors.Address _address = stage.addressFactory().uniquePrefixedWith("g-");
					final Behavior<STATE> behavior = stage.actorFor(Behavior.class,
							Definition.has(EventSourcedAggregateBehavior.class,
									Definition.parameters(_address.idString(), aggregateSupplier)),
							_address);
					return reactTo(behavior, command);
				}
			}
		}
	}
}
