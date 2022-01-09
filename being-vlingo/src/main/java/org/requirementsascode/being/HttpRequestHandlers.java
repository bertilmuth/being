package org.requirementsascode.being;

import static io.vlingo.xoom.common.serialization.JsonSerialization.serialized;
import static io.vlingo.xoom.http.Response.Status.Created;
import static io.vlingo.xoom.http.Response.Status.InternalServerError;
import static io.vlingo.xoom.http.Response.Status.NotFound;
import static io.vlingo.xoom.http.Response.Status.Ok;
import static io.vlingo.xoom.http.resource.ResourceBuilder.get;
import static io.vlingo.xoom.http.resource.ResourceBuilder.patch;
import static io.vlingo.xoom.http.resource.ResourceBuilder.post;
import static io.vlingo.xoom.http.resource.ResourceBuilder.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import io.vlingo.xoom.actors.Address;
import io.vlingo.xoom.actors.Definition;
import io.vlingo.xoom.actors.Stage;
import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.http.ContentType;
import io.vlingo.xoom.http.Response;
import io.vlingo.xoom.http.resource.DynamicResourceHandler;
import io.vlingo.xoom.http.resource.RequestHandler;
import io.vlingo.xoom.http.resource.RequestHandler0;
import io.vlingo.xoom.http.resource.RequestHandler1;
import io.vlingo.xoom.http.resource.RequestHandler2;
import io.vlingo.xoom.http.resource.Resource;
import io.vlingo.xoom.turbo.ComponentRegistry;

public class HttpRequestHandlers<STATE, DATA> extends DynamicResourceHandler {
	private final Stage stage;
	private final Queries<DATA> queries;
	private final List<RequestHandler> httpRequestHandlers;
	private final Supplier<EventSourcedAggregate<STATE>> aggregateSupplier;
	private final EventSourcedAggregate<STATE> aggregate;
	private final Function<STATE, DATA> dataFromState;
	private final String resourceName;

	HttpRequestHandlers(final Stage stage, Supplier<EventSourcedAggregate<STATE>> aggregateSupplier,
			Function<STATE, DATA> dataFromState) {
		super(stage.world().stage());
		this.stage = stage;
		this.httpRequestHandlers = new ArrayList<>();
		this.aggregateSupplier = aggregateSupplier;
		this.aggregate = aggregateSupplier.get();
		this.queries = queriesFor(aggregate, dataFromState);
		this.resourceName = resourceNameOf(aggregate);
		this.dataFromState = dataFromState;
	}

	public static HttpRequestHandlersBuilder builder() {
		return new HttpRequestHandlersBuilder();
	}

	@Override
	public Resource<?> routes() {
		RequestHandler[] requestHandlerArray = httpRequestHandlers.toArray(new RequestHandler[httpRequestHandlers.size()]);
		return resource(resourceName, requestHandlerArray);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Completes<STATE> reactTo(Behavior behavior, Object command) {
		return behavior.reactTo(command);
	}

	void createRequest(String url, Class<?> createRequestClass) {
		final RequestHandler1<?> handler = post(url).body(createRequestClass).handle(this::createAggregate);
		httpRequestHandlers.add(handler);
	}

	void updateRequest(String url, Class<?> updateRequestClass) {
		final RequestHandler2<String, ?> handler = patch(url).param(String.class).body(updateRequestClass)
				.handle(this::updateAggregate);
		httpRequestHandlers.add(handler);
	}

	void findByIdRequest(String url) {
		final RequestHandler1<String> handler = get(url).param(String.class).handle(this::findAggregateById);
		httpRequestHandlers.add(handler);
	}

	void findAllRequest(String url) {
		final RequestHandler0 handler = get(url).handle(this::findAllAggregates);
		httpRequestHandlers.add(handler);
	}

	private Completes<Response> createAggregate(Object request) {
		return createAggregateOnStage(stage, request).andThenTo(state -> {
			return Completes.withSuccess(entityResponseOf(Created, serialized(dataFromState.apply(state))))
					.otherwise(arg -> Response.of(NotFound))
					.recoverFrom(e -> Response.of(InternalServerError, e.getMessage()));
		});

	}

	private Completes<Response> updateAggregate(final String id, final Object request) {
		return resolve(id).andThenTo(behavior -> reactTo(behavior, request))
				.andThenTo(state -> Completes.withSuccess(entityResponseOf(Ok, serialized(dataFromState.apply(state)))))
				.otherwise(noData -> Response.of(NotFound))
				.recoverFrom(e -> Response.of(InternalServerError, e.getMessage()));
	}

	private Completes<Response> findAllAggregates() {
		Completes<Collection<DATA>> findAll = queries.findAll();
		return findAll.andThenTo(data -> Completes.withSuccess(entityResponseOf(Ok, serialized(data))))
				.otherwise(arg -> Response.of(NotFound))
				.recoverFrom(e -> Response.of(InternalServerError, e.getMessage()));
	}

	private Completes<Response> findAggregateById(final String id) {
		Completes<DATA> findById = queries.findById(id);
		return findById.andThenTo(data -> Completes.withSuccess(entityResponseOf(Ok, serialized(data))))
				.otherwise(arg -> Response.of(NotFound))
				.recoverFrom(e -> Response.of(InternalServerError, e.getMessage()));
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

	@Override
	protected ContentType contentType() {
		return ContentType.of("application/json", "charset=UTF-8");
	}

	private Queries<DATA> queriesFor(EventSourcedAggregate<STATE> aggregate, Function<STATE, DATA> dataFromState) {
		Class<? extends Object> dataTypeOfAggregate = dataTypeOfAggregate(aggregate, dataFromState);
		return queriesByDataType(dataTypeOfAggregate);
	}

	private Class<? extends Object> dataTypeOfAggregate(EventSourcedAggregate<STATE> aggregate,
			Function<STATE, DATA> dataFromState) {
		Class<? extends Object> aggregateDataClass = dataFromState.apply(aggregate.initialState(null)).getClass();
		return aggregateDataClass;
	}

	@SuppressWarnings("unchecked")
	private Queries<DATA> queriesByDataType(Class<? extends Object> dataType) {
		return (Queries<DATA>) ComponentRegistry.withType(QueryModelStateStoreProvider.class).queriesByDataType
				.get(dataType);
	}
	
	private String resourceNameOf(EventSourcedAggregate<STATE> aggregate) {
		String resourceName = aggregate.getClass().getSimpleName() + "RequestHandlers";
		return resourceName;
	}
}
