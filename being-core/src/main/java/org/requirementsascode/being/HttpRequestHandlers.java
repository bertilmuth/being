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
import static java.util.Objects.*;

public class HttpRequestHandlers<CMD, STATE, DATA> extends DynamicResourceHandler {
	private final Stage currentStage;
	private final Queries<DATA> queries;
	private final List<RequestHandler> requestHandlers;
	private final Supplier<AggregateBehavior<CMD, STATE>> behaviorSupplier;
	private final Function<STATE, DATA> dataFromState;
	private final String resourceName;

	HttpRequestHandlers(final Stage currentStage, final Supplier<AggregateBehavior<CMD, STATE>> behaviorSupplier,
		final Function<STATE, DATA> dataFromState) {

		super(currentStage.world().stage());
		this.currentStage = requireNonNull(currentStage, "currentStage must be non-null!");
		this.behaviorSupplier = requireNonNull(behaviorSupplier, "behaviorSupplier must be non-null!");
		
		final AggregateBehavior<CMD, STATE> aggregate = behaviorSupplier.get();
		this.queries = queriesFor(aggregate, dataFromState);
		this.resourceName = resourceNameOf(aggregate);
		
		this.dataFromState = dataFromState;
		this.requestHandlers = new ArrayList<>();
	}

	public static HttpRequestHandlersBuilder builder() {
		return new HttpRequestHandlersBuilder();
	}

	@Override
	public Resource<?> routes() {
		final RequestHandler[] requestHandlerArray = requestHandlers()
			.toArray(new RequestHandler[requestHandlers().size()]);
		return resource(resourceName(), requestHandlerArray);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Completes<STATE> reactTo(final Aggregate behavior, final CMD command) {
		requireNonNull(behavior, "behavior must be non-null!");
		requireNonNull(command, "command must be non-null!");

		return behavior.reactTo(command);
	}

	void createRequest(final String url, final Class<? extends CMD> createRequestClass) {
		requireNonNull(url, "url must be non-null!");
		requireNonNull(createRequestClass, "createRequestClass must be non-null!");

		final RequestHandler1<? extends CMD> handler = post(url).body(createRequestClass).handle(this::createAggregate);
		requestHandlers().add(handler);
	}

	void updateRequest(final String url, final Class<? extends CMD> updateRequestClass) {
		requireNonNull(url, "url must be non-null!");
		requireNonNull(updateRequestClass, "updateRequestClass must be non-null!");

		final RequestHandler2<String, ? extends CMD> handler = patch(url).param(String.class).body(updateRequestClass)
			.handle(this::updateAggregate);
		requestHandlers().add(handler);
	}

	void findByIdRequest(final String url) {
		requireNonNull(url, "url must be non-null!");

		final RequestHandler1<String> handler = get(url).param(String.class).handle(this::findAggregateById);
		requestHandlers().add(handler);
	}

	void findAllRequest(final String url) {
		requireNonNull(url, "url must be non-null!");

		final RequestHandler0 handler = get(url).handle(this::findAllAggregates);
		requestHandlers().add(handler);
	}

	@SuppressWarnings("rawtypes")
	private Completes<Aggregate> resolve(final String id) {
		requireNonNull(id, "id must be non-null!");

		final Address address = currentStage().addressFactory().from(id);
		final Completes<Aggregate> actor = currentStage().actorOf(Aggregate.class, address,
			Definition.has(EventSourcedAggregate.class, Definition.parameters(id, behaviorSupplier())));
		logger().info("Resolved actor: " + actor.id());
		return actor;
	}

	private Completes<Response> createAggregate(final CMD request) {
		requireNonNull(request, "request must be non-null!");

		return createAggregateOnStage(currentStage(), request).andThenTo(state -> {
			return Completes.withSuccess(entityResponseOf(Created, serialized(dataFromState().apply(state))))
				.otherwise(arg -> Response.of(NotFound)).recoverFrom(e -> Response.of(InternalServerError, e.getMessage()));
		});
	}

	private Completes<Response> updateAggregate(final String id, final CMD request) {
		requireNonNull(id, "id must be non-null!");
		requireNonNull(request, "request must be non-null!");

		return resolve(id).andThenTo(behavior -> reactTo(behavior, request))
			.andThenTo(state -> Completes.withSuccess(entityResponseOf(Ok, serialized(dataFromState().apply(state)))))
			.otherwise(noData -> Response.of(NotFound)).recoverFrom(e -> Response.of(InternalServerError, e.getMessage()));
	}

	private Completes<Response> findAllAggregates() {
		final Completes<Collection<DATA>> findAll = queries().findAll();
		return findAll.andThenTo(data -> Completes.withSuccess(entityResponseOf(Ok, serialized(data))))
			.otherwise(arg -> Response.of(NotFound)).recoverFrom(e -> Response.of(InternalServerError, e.getMessage()));
	}

	private Completes<Response> findAggregateById(final String id) {

		final Completes<DATA> findById = queries().findById(id);
		return findById.andThenTo(data -> Completes.withSuccess(entityResponseOf(Ok, serialized(data))))
			.otherwise(arg -> Response.of(NotFound)).recoverFrom(e -> Response.of(InternalServerError, e.getMessage()));
	}

	@SuppressWarnings("unchecked")
	private Completes<STATE> createAggregateOnStage(final Stage stage, final CMD command) {
		requireNonNull(stage, "stage must be non-null!");
		requireNonNull(command, "command must be non-null!");

		final io.vlingo.xoom.actors.Address _address = stage.addressFactory().uniquePrefixedWith("b-");
		final Aggregate<CMD, STATE> behavior = stage.actorFor(Aggregate.class,
			Definition.has(EventSourcedAggregate.class, Definition.parameters(_address.idString(), behaviorSupplier())),
			_address);
		return reactTo(behavior, command);
	}

	@Override
	protected ContentType contentType() {
		return ContentType.of("application/json", "charset=UTF-8");
	}

	private Queries<DATA> queriesFor(final AggregateBehavior<CMD, STATE> aggregate, final Function<STATE, DATA> queryDataFromState) {
		requireNonNull(aggregate, "aggregate must be non-null!");
		requireNonNull(queryDataFromState, "queryDataFromState must be non-null!");

		final Class<? extends Object> queryDataType = queryDataType(aggregate, queryDataFromState);
		final Queries<DATA> queries = queriesForDataType(queryDataType);
		if (queries == null) {
			throw new IllegalArgumentException("no query model data found for type " + queryDataType.getName() + "!");
		}

		return queries;
	}

	private Class<? extends Object> queryDataType(final AggregateBehavior<CMD, STATE> aggregate, final Function<STATE, DATA> queryDataFromState) {
		requireNonNull(aggregate, "aggregate must be non-null!");
		requireNonNull(queryDataFromState, "queryDataFromState must be non-null!");

		final DATA dataFromEmptyState = queryDataFromState.apply(aggregate.initialState(""));
		requireNonNull(dataFromEmptyState, "transforming empty aggregate state to query data must be non-null!");

		final Class<? extends Object> aggregateDataClass = dataFromEmptyState.getClass();
		return aggregateDataClass;
	}

	@SuppressWarnings("unchecked")
	private Queries<DATA> queriesForDataType(final Class<? extends Object> dataType) {
		requireNonNull(dataType, "dataType must be non-null!");

		return (Queries<DATA>) ComponentRegistry.withType(QueryModelStateStoreProvider.class).queriesByDataTypeMap
			.get(dataType);
	}

	private String resourceNameOf(AggregateBehavior<CMD, STATE> aggregate) {
		requireNonNull(aggregate, "aggregate must be non-null!");

		final String resourceName = aggregate.getClass().getSimpleName() + "RequestHandlers";
		return resourceName;
	}

	public Stage currentStage() {
		return currentStage;
	}

	public List<RequestHandler> requestHandlers() {
		return requestHandlers;
	}

	public Supplier<AggregateBehavior<CMD, STATE>> behaviorSupplier() {
		return behaviorSupplier;
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
