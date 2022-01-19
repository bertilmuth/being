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
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import org.requirementsascode.being.EventSourcedAggregate.Instantiator;

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

/**
 * This class defines the HTTP routes that accept POST, PATCH and GET HTTP requests for an aggregate,
 * creates the necessary infrastructure and calls the command handlers of the aggregate
 * and query handlers of a query model, to handle the requests.
 * 
 * @author b_muth
 *
 * @param <CMD> the type of command handled by the request handlers
 * @param <STATE> the type of state of the aggregate
 * @param <DATA> the type of data of the query model
 */
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
		
		final AggregateBehavior<CMD, STATE> aggregate = requireNonNull(behaviorSupplier.get(), "behaviorSupplier must return non-null value!");
		this.queries = queriesFor(aggregate, dataFromState);
		this.resourceName = resourceNameOf(aggregate);
		
		this.dataFromState = requireNonNull(dataFromState, "dataFromState must be non-null!");
		this.requestHandlers = new ArrayList<>();
	}

	/**
	 * Create a builder for HTTP request handlers.
	 * 
	 * @return the builder
	 */
	public static HttpRequestHandlersBuilder builder() {
		return new HttpRequestHandlersBuilder();
	}

	/**
	 * Returns the HTTP routes that have been built.
	 * You can use the to configure a VLINGO server.
	 * 
	 * @return the HTTP routes
	 */
	@Override
	public Resource<?> routes() {
		RequestHandler[] requestHandlerArray = requestHandlers()
			.toArray(new RequestHandler[requestHandlers().size()]);
		return resource(resourceName(), requestHandlerArray);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Completes<STATE> reactTo(final Aggregate behavior, final CMD command) {
		return behavior.reactTo(command);
	}

	void createRequest(final String url, final Class<? extends CMD> createRequestClass) {
		final RequestHandler1<? extends CMD> handler = post(url).body(createRequestClass).handle(this::createAggregate);
		requestHandlers().add(handler);
	}

	void updateRequest(final String url, final Class<? extends CMD> updateRequestClass) {
		RequestHandler2<String, ? extends CMD> handler = patch(url).param(String.class).body(updateRequestClass)
			.handle(this::updateAggregate);
		requestHandlers().add(handler);
	}

	void findByIdRequest(final String url) {
		RequestHandler1<String> handler = get(url).param(String.class).handle(this::findAggregateById);
		requestHandlers().add(handler);
	}

	void findAllRequest(final String url) {
		RequestHandler0 handler = get(url).handle(this::findAllAggregates);
		requestHandlers().add(handler);
	}

	@SuppressWarnings("rawtypes")
	private Completes<Aggregate> resolve(final String id) {
		Address address = currentStage().addressFactory().from(id);

		Instantiator<CMD, STATE> instantiator = new EventSourcedAggregate.Instantiator<>(id, behaviorSupplier());

		Completes<Aggregate> actor = currentStage().actorOf(Aggregate.class, address,
			Definition.has(EventSourcedAggregate.class, instantiator));
		return actor;
	}

	private Completes<Response> createAggregate(final CMD request) {
		return createAggregateOnStage(currentStage(), request).andThenTo(state -> {
			return Completes.withSuccess(entityResponseOf(Created, serialized(dataFromState().apply(state))))
				.otherwise(arg -> Response.of(NotFound)).recoverFrom(e -> Response.of(InternalServerError, e.getMessage()));
		});
	}

	private Completes<Response> updateAggregate(final String id, final CMD request) {
		return resolve(id).andThenTo(behavior -> reactTo(behavior, request))
			.andThenTo(state -> Completes.withSuccess(entityResponseOf(Ok, serialized(dataFromState().apply(state)))))
			.otherwise(noData -> Response.of(NotFound)).recoverFrom(e -> Response.of(InternalServerError, e.getMessage()));
	}

	private Completes<Response> findAllAggregates() {
		Completes<Collection<DATA>> findAll = queries().findAll();
		return findAll.andThenTo(data -> Completes.withSuccess(entityResponseOf(Ok, serialized(data))))
			.otherwise(arg -> Response.of(NotFound)).recoverFrom(e -> Response.of(InternalServerError, e.getMessage()));
	}

	private Completes<Response> findAggregateById(final String id) {
		Completes<DATA> findById = queries().findById(id);
		return findById.andThenTo(data -> Completes.withSuccess(entityResponseOf(Ok, serialized(data))))
			.otherwise(arg -> Response.of(NotFound)).recoverFrom(e -> Response.of(InternalServerError, e.getMessage()));
	}

	@SuppressWarnings("unchecked")
	private Completes<STATE> createAggregateOnStage(final Stage stage, final CMD command) {
		Address _address = stage.addressFactory().uniquePrefixedWith("b-");
		Instantiator<CMD, STATE> instantiator = new EventSourcedAggregate.Instantiator<>(_address.idString(),
			behaviorSupplier());
		Aggregate<CMD, STATE> behavior = stage.actorFor(Aggregate.class,
			Definition.has(EventSourcedAggregate.class, instantiator));
		return reactTo(behavior, command);
	}

	@Override
	protected ContentType contentType() {
		return ContentType.of("application/json", "charset=UTF-8");
	}

	private Queries<DATA> queriesFor(final AggregateBehavior<CMD, STATE> aggregate, final Function<STATE, DATA> queryDataFromState) {
		Class<? extends Object> queryDataType = queryDataType(aggregate, queryDataFromState);
		Queries<DATA> queries = queriesForDataType(queryDataType);
		if (queries == null) {
			throw new IllegalArgumentException("no query model data found for type " + queryDataType.getName() + "!");
		}

		return queries;
	}

	private Class<? extends Object> queryDataType(final AggregateBehavior<CMD, STATE> aggregate, final Function<STATE, DATA> queryDataFromState) {
		DATA dataFromEmptyState = queryDataFromState.apply(aggregate.initialState(""));
		requireNonNull(dataFromEmptyState, "transforming empty aggregate state to query data must be non-null!");

		Class<? extends Object> aggregateDataClass = dataFromEmptyState.getClass();
		return aggregateDataClass;
	}

	@SuppressWarnings("unchecked")
	private Queries<DATA> queriesForDataType(final Class<? extends Object> dataType) {
		return (Queries<DATA>) ComponentRegistry.withType(QueryModelStateStoreProvider.class).queriesByDataTypeMap
			.get(dataType);
	}

	private String resourceNameOf(AggregateBehavior<CMD, STATE> aggregate) {
		String resourceName = aggregate.getClass().getSimpleName() + "RequestHandlers";
		return resourceName;
	}

	private Stage currentStage() {
		return currentStage;
	}

	private List<RequestHandler> requestHandlers() {
		return requestHandlers;
	}

	private Supplier<AggregateBehavior<CMD, STATE>> behaviorSupplier() {
		return behaviorSupplier;
	}

	private Queries<DATA> queries() {
		return queries;
	}

	private String resourceName() {
		return resourceName;
	}

	private Function<STATE, DATA> dataFromState() {
		return dataFromState;
	}
}
