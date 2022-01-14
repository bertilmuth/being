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
import java.util.Objects;
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

public class HttpRequestHandlers<CMD, STATE, DATA> extends DynamicResourceHandler {
	private final Stage currentStage;
	private final Queries<DATA> queries;
	private final List<RequestHandler> requestHandlers;
	private final Supplier<EventSourcedAggregate<CMD, STATE>> aggregateSupplier;
	private final Function<STATE, DATA> dataFromState;
	private final String resourceName;

	HttpRequestHandlers(final Stage currentStage, final Supplier<EventSourcedAggregate<CMD, STATE>> aggregateSupplier,
			final Function<STATE, DATA> dataFromState) {
		super(currentStage.world().stage());
		this.currentStage = currentStage;
		this.requestHandlers = new ArrayList<>();
		this.aggregateSupplier = aggregateSupplier;
		EventSourcedAggregate<CMD, STATE> aggregate = aggregateSupplier.get();
		this.queries = queriesFor(aggregate, dataFromState);
		this.resourceName = resourceNameOf(aggregate);
		this.dataFromState = dataFromState;
	}

	public static HttpRequestHandlersBuilder builder() {
		return new HttpRequestHandlersBuilder();
	}

	@Override
	public Resource<?> routes() {
		RequestHandler[] requestHandlerArray = requestHandlers().toArray(new RequestHandler[requestHandlers().size()]);
		return resource(resourceName(), requestHandlerArray);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Completes<STATE> reactTo(final AggregateBehavior behavior, final CMD command) {
		return behavior.reactTo(command);
	}

	void createRequest(String url, Class<? extends CMD> createRequestClass) {
		final RequestHandler1<? extends CMD> handler = post(url).body(createRequestClass).handle(this::createAggregate);
		requestHandlers().add(handler);
	}

	void updateRequest(String url, Class<? extends CMD> updateRequestClass) {
		final RequestHandler2<String, ? extends CMD> handler = patch(url).param(String.class).body(updateRequestClass)
				.handle(this::updateAggregate);
		requestHandlers().add(handler);
	}

	void findByIdRequest(String url) {
		final RequestHandler1<String> handler = get(url).param(String.class).handle(this::findAggregateById);
		requestHandlers().add(handler);
	}

	void findAllRequest(String url) {
		final RequestHandler0 handler = get(url).handle(this::findAllAggregates);
		requestHandlers().add(handler);
	}
	
	@SuppressWarnings("rawtypes")
	private Completes<AggregateBehavior> resolve(final String id) {
		final Address address = currentStage().addressFactory().from(id);
		final Completes<AggregateBehavior> actor = currentStage().actorOf(AggregateBehavior.class, address,
				Definition.has(EventSourcedAggregateBehavior.class, Definition.parameters(id, aggregateSupplier())));
		logger().info("Resolved actor: " + actor.id());
		return actor;
	}

	private Completes<Response> createAggregate(CMD request) {
		return createAggregateOnStage(currentStage(), request).andThenTo(state -> {
			return Completes.withSuccess(entityResponseOf(Created, serialized(dataFromState().apply(state))))
					.otherwise(arg -> Response.of(NotFound))
					.recoverFrom(e -> Response.of(InternalServerError, e.getMessage()));
		});
	}

	private Completes<Response> updateAggregate(final String id, final CMD request) {
		return resolve(id).andThenTo(behavior -> reactTo(behavior, request))
				.andThenTo(state -> Completes.withSuccess(entityResponseOf(Ok, serialized(dataFromState().apply(state)))))
				.otherwise(noData -> Response.of(NotFound))
				.recoverFrom(e -> Response.of(InternalServerError, e.getMessage()));
	}

	private Completes<Response> findAllAggregates() {
		Completes<Collection<DATA>> findAll = queries().findAll();
		return findAll.andThenTo(data -> Completes.withSuccess(entityResponseOf(Ok, serialized(data))))
				.otherwise(arg -> Response.of(NotFound))
				.recoverFrom(e -> Response.of(InternalServerError, e.getMessage()));
	}

	private Completes<Response> findAggregateById(final String id) {
		Completes<DATA> findById = queries().findById(id);
		return findById.andThenTo(data -> Completes.withSuccess(entityResponseOf(Ok, serialized(data))))
				.otherwise(arg -> Response.of(NotFound))
				.recoverFrom(e -> Response.of(InternalServerError, e.getMessage()));
	}

	@SuppressWarnings("unchecked")
	private Completes<STATE> createAggregateOnStage(final Stage stage, final CMD command) {
		final io.vlingo.xoom.actors.Address _address = stage.addressFactory().uniquePrefixedWith("b-");
		final AggregateBehavior<CMD, STATE> behavior = stage.actorFor(AggregateBehavior.class,
				Definition.has(EventSourcedAggregateBehavior.class,
						Definition.parameters(_address.idString(), aggregateSupplier())),
				_address);
		return reactTo(behavior, command);
	}

	@Override
	protected ContentType contentType() {
		return ContentType.of("application/json", "charset=UTF-8");
	}

	private Queries<DATA> queriesFor(EventSourcedAggregate<CMD, STATE> aggregate, Function<STATE, DATA> queryDataFromState) {
		Class<? extends Object> queryDataType = queryDataType(aggregate, queryDataFromState);
		final Queries<DATA> queries = queriesForDataType(queryDataType);
		if(queries == null) {
			throw new IllegalArgumentException("no query model data found for type " + queryDataType.getName() + "!");
		}
		
		return queries;
	}

	private Class<? extends Object> queryDataType(EventSourcedAggregate<CMD, STATE> aggregate,
			Function<STATE, DATA> queryDataFromState) {
		final DATA dataFromEmptyState = queryDataFromState.apply(aggregate.initialState(""));
		Objects.requireNonNull(dataFromEmptyState, "transforming empty aggregate state to query data must be non-null!"); 
		
		Class<? extends Object> aggregateDataClass = dataFromEmptyState.getClass();
		return aggregateDataClass;
	}

	@SuppressWarnings("unchecked")
	private Queries<DATA> queriesForDataType(Class<? extends Object> dataType) {
		return (Queries<DATA>) ComponentRegistry.withType(QueryModelStateStoreProvider.class).queriesByDataTypeMap
				.get(dataType);
	}
	
	private String resourceNameOf(EventSourcedAggregate<CMD, STATE> aggregate) {
		String resourceName = aggregate.getClass().getSimpleName() + "RequestHandlers";
		return resourceName;
	}

	public Stage currentStage() {
		return currentStage;
	}

	public List<RequestHandler> requestHandlers() {
		return requestHandlers;
	}

	public Supplier<EventSourcedAggregate<CMD, STATE>> aggregateSupplier() {
		return aggregateSupplier;
	}
	
	public Queries<DATA> queries() {
		return queries;
	}
	
	public String resourceName() {
		return resourceName;
	}

	public Function<STATE, DATA> dataFromState() {
		return dataFromState;
	}
}
