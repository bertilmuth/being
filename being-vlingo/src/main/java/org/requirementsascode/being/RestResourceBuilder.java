package org.requirementsascode.being;

import static io.vlingo.xoom.common.serialization.JsonSerialization.serialized;
import static io.vlingo.xoom.http.Response.Status.Created;
import static io.vlingo.xoom.http.Response.Status.InternalServerError;
import static io.vlingo.xoom.http.Response.Status.NotFound;
import static io.vlingo.xoom.http.Response.Status.Ok;
import static io.vlingo.xoom.http.resource.ResourceBuilder.get;
import static io.vlingo.xoom.http.resource.ResourceBuilder.patch;
import static io.vlingo.xoom.http.resource.ResourceBuilder.post;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import io.vlingo.xoom.actors.Address;
import io.vlingo.xoom.actors.Definition;
import io.vlingo.xoom.actors.Stage;
import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.http.Response;
import io.vlingo.xoom.http.resource.RequestHandler;
import io.vlingo.xoom.lattice.grid.Grid;

public class RestResourceBuilder {
	RestResourceBuilder(){
	}
	
	public GridBuilder grid(Grid grid) {
		return new GridBuilder(grid);
	}
	
	public class GridBuilder{
		private final Grid grid;

		GridBuilder(Grid grid) {
			this.grid = grid;
		}
		
		public <STATE> AggregateBuilder<STATE> aggregate(EventSourcedAggregate<STATE> aggregate){
			return new AggregateBuilder<>(grid, aggregate);
		}
	}
	
	public class AggregateBuilder<STATE>{
		private final Grid grid;
		private final EventSourcedAggregate<STATE> aggregate;

		AggregateBuilder(Grid grid, EventSourcedAggregate<STATE> aggregate) {
			this.grid = grid;
			this.aggregate = aggregate;
		}
		
		public <DATA> StateToDataMapperBuilder<DATA> stateToDataMapper(Function<STATE, DATA> stateToDataMapper){
			return new StateToDataMapperBuilder<DATA>(stateToDataMapper);
		}
		
		public class StateToDataMapperBuilder<DATA>{
			private final Function<STATE, DATA> stateToDataMapper;
			private final List<RequestHandler> requestHandlers;
			private final RestResource<DATA> restResource;

			public StateToDataMapperBuilder(Function<STATE, DATA> stateToDataMapper) {
				this.stateToDataMapper = stateToDataMapper;
				this.requestHandlers = new ArrayList<>();
				this.restResource = new RestResource<>(grid, resourceName());
			}
			
			private String resourceName() {
				return aggregate.getClass().getSimpleName() + "Resource";
			}
			
			public AggregateBuilder<STATE>.StateToDataMapperBuilder<DATA> create(String url, Class<?> createCommandClass){
				requestHandlers.add(post(url).body(createCommandClass).handle(this::createAggregate));
				return this;
			}
			
			public AggregateBuilder<STATE>.StateToDataMapperBuilder<DATA> update(String url, Class<?> updateCommandClass){
				requestHandlers.add(patch(url).param(String.class).body(updateCommandClass).handle(this::updateAggregate));
				return this;
			}
			
			public AggregateBuilder<STATE>.StateToDataMapperBuilder<DATA> findById(String url){
				requestHandlers.add(get(url).param(String.class).handle(this::findAggregateById));
				return this;
			}
			
			public AggregateBuilder<STATE>.StateToDataMapperBuilder<DATA> findAll(String url){
				requestHandlers.add(get(url).handle(this::findAllAggregates));
				return this;
			}
			
			public RestResource<DATA> build(){
				restResource.setRequestHandlers(requestHandlers);
				return restResource;
			}
			
			private Completes<Response> createAggregate(Object command) {
				return createAggregateOnStage(grid, command).andThenTo(state -> {
					return Completes
							.withSuccess(restResource.responseOf(Created, serialized(stateToDataMapper.apply(state))))
							.otherwise(arg -> Response.of(NotFound))
							.recoverFrom(e -> Response.of(InternalServerError, e.getMessage()));
				});

			}

			private Completes<Response> updateAggregate(final String id, final Object command) {
				return resolve(id).andThenTo(behavior -> reactTo(behavior, command))
						.andThenTo(state -> Completes.withSuccess(restResource.responseOf(Ok, serialized(stateToDataMapper.apply(state)))))
						.otherwise(noData -> Response.of(NotFound))
						.recoverFrom(e -> Response.of(InternalServerError, e.getMessage()));
			}

			private Completes<Response> findAllAggregates() {
				Completes<Collection<DATA>> findAll = restResource.$queries().findAll();
				return findAll.andThenTo(data -> Completes.withSuccess(restResource.responseOf(Ok, serialized(data))))
						.otherwise(arg -> Response.of(NotFound))
						.recoverFrom(e -> Response.of(InternalServerError, e.getMessage()));
			}

			private Completes<Response> findAggregateById(final String id) {
				Completes<DATA> findById = restResource.$queries().findById(id);
				return findById.andThenTo(data -> Completes.withSuccess(restResource.responseOf(Ok, serialized(data))))
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
				final Behavior<STATE> behavior = stage.actorFor(Behavior.class,
						Definition.has(EventSourcedBehavior.class, Definition.parameters(_address.idString(), aggregate)), _address);
				return reactTo(behavior, command);
			}
		}
	}

}
