package org.requirementsascode.being;

import static io.vlingo.xoom.http.resource.ResourceBuilder.resource;

import java.util.List;
import java.util.Objects;

import io.vlingo.xoom.http.ContentType;
import io.vlingo.xoom.http.Response;
import io.vlingo.xoom.http.Response.Status;
import io.vlingo.xoom.http.resource.DynamicResourceHandler;
import io.vlingo.xoom.http.resource.RequestHandler;
import io.vlingo.xoom.http.resource.Resource;
import io.vlingo.xoom.lattice.grid.Grid;
import io.vlingo.xoom.turbo.ComponentRegistry;

public class RestResource<DATA> extends DynamicResourceHandler {
	private final String resourceName;
	private final Queries<DATA> $queries;
	private List<RequestHandler> requestHandlers;

	@SuppressWarnings("unchecked")
	public RestResource(final Grid grid, String resourceName) {
		super(grid.world().stage());
		this.resourceName = Objects.requireNonNull(resourceName, "aggregate must be non-null!");
		this.$queries = ComponentRegistry.withType(QueryModelStateStoreProvider.class).queries;
	}
	
	public static RestResourceBuilder builder() {
		return new RestResourceBuilder();
	}
	
	void setRequestHandlers(List<RequestHandler> requestHandlers) {
		this.requestHandlers = requestHandlers;
	}

	@Override
	public Resource<?> routes() {		
		return resource(resourceName(), requestHandlerArray());
	}
	
	private String resourceName() {
		return resourceName; // aggregate.getClass().getSimpleName() + "Resource";
	}
	
	Queries<DATA> $queries(){
		return $queries;
	}
	
	Response responseOf(final Status status, final String entity) {
		return super.entityResponseOf(status, entity);
	}
	
	@Override
	protected ContentType contentType() {
		return ContentType.of("application/json", "charset=UTF-8");
	}

	private RequestHandler[] requestHandlerArray() {
		return requestHandlers.toArray(new RequestHandler[requestHandlers.size()]);
	}
}
