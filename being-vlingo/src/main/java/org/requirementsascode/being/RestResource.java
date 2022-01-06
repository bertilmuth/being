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

import io.vlingo.xoom.actors.Address;
import io.vlingo.xoom.actors.Definition;
import io.vlingo.xoom.actors.Stage;
import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.http.ContentType;
import io.vlingo.xoom.http.Response;
import io.vlingo.xoom.http.resource.DynamicResourceHandler;
import io.vlingo.xoom.http.resource.RequestHandler;
import io.vlingo.xoom.http.resource.Resource;
import io.vlingo.xoom.lattice.grid.Grid;
import io.vlingo.xoom.turbo.ComponentRegistry;

public class RestResource<STATE, DATA> extends DynamicResourceHandler {
	private final Grid grid;
	private final Queries<DATA> $queries;
	private final Aggregate<STATE> aggregate;
	private final List<RequestHandler> requestHandlers;
	private final ViewModel<STATE, DATA> viewModel;

	@SuppressWarnings("unchecked")
	public RestResource(final Grid grid, final Aggregate<STATE> aggregate, ViewModel<STATE, DATA> viewModel) {
		super(grid.world().stage());
		this.grid = Objects.requireNonNull(grid, "grid must be non-null!");
		this.aggregate = Objects.requireNonNull(aggregate, "aggregate must be non-null!");
		this.viewModel = Objects.requireNonNull(viewModel, "viewModel must be non-null!");
		this.$queries = ComponentRegistry.withType(QueryModelStateStoreProvider.class).queries;
		this.requestHandlers = new ArrayList<>();
	}

	private Completes<Response> createAggregate(Object command) {
		return createAggregateOnStage(grid, command).andThenTo(state -> Completes
				.withSuccess(entityResponseOf(Created,
						serialized(viewModel.stateToData().apply(state))))
				.otherwise(arg -> Response.of(NotFound))
				.recoverFrom(e -> Response.of(InternalServerError, e.getMessage())));

	}

	private Completes<Response> updateAggregate(final String id, final Object command) {
		return resolve(id).andThenTo(behavior -> reactTo(behavior, command))
				.andThenTo(state -> Completes.withSuccess(entityResponseOf(Ok, serialized(viewModel.stateToData().apply(state)))))
				.otherwise(noData -> Response.of(NotFound))
				.recoverFrom(e -> Response.of(InternalServerError, e.getMessage()));
	}

	private Completes<Response> findAllAggregates() {
		Completes<Collection<DATA>> findAll = $queries.findAll();
		return findAll.andThenTo(data -> Completes.withSuccess(entityResponseOf(Ok, serialized(data))))
				.otherwise(arg -> Response.of(NotFound))
				.recoverFrom(e -> Response.of(InternalServerError, e.getMessage()));
	}

	private Completes<Response> findAggregateById(final String id) {
		Completes<DATA> findById = $queries.findById(id);
		return findById.andThenTo(data -> Completes.withSuccess(entityResponseOf(Ok, serialized(data))))
				.otherwise(arg -> Response.of(NotFound))
				.recoverFrom(e -> Response.of(InternalServerError, e.getMessage()));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Completes<STATE> reactTo(Behavior behavior, Object command) {
		return behavior.reactTo(command);
	}

	@Override
	public Resource<?> routes() {		
		return resource(resourceName(), requestHandlerArray());
	}
	
	public RestResource<STATE, DATA> create(String url, Class<?> createCommandClass){
		requestHandlers.add(post(url).body(createCommandClass).handle(this::createAggregate));
		return this;
	}
	
	public RestResource<STATE, DATA> update(String url, Class<?> updateCommandClass){
		requestHandlers.add(patch(url).param(String.class).body(updateCommandClass).handle(this::updateAggregate));
		return this;
	}
	
	public RestResource<STATE, DATA> findById(String url){
		requestHandlers.add(get(url).param(String.class).handle(this::findAggregateById));
		return this;
	}
	
	public RestResource<STATE, DATA> findAll(String url){
		requestHandlers.add(get(url).handle(this::findAllAggregates));
		return this;
	}

	private String resourceName() {
		return aggregate.getClass().getSimpleName() + "Resource";
	}

	@Override
	protected ContentType contentType() {
		return ContentType.of("application/json", "charset=UTF-8");
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
		final Behavior<STATE> behavior = stage.actorFor(Behavior.class,
				Definition.has(EventSourcedBehavior.class, Definition.parameters(_address.idString(), aggregate)), _address);
		return reactTo(behavior, command);
	}
	
	private RequestHandler[] requestHandlerArray() {
		return requestHandlers.toArray(new RequestHandler[requestHandlers.size()]);
	}
}
