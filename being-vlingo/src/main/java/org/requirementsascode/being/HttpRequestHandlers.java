package org.requirementsascode.being;

import static io.vlingo.xoom.http.resource.ResourceBuilder.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.vlingo.xoom.actors.Stage;
import io.vlingo.xoom.http.ContentType;
import io.vlingo.xoom.http.Response;
import io.vlingo.xoom.http.Response.Status;
import io.vlingo.xoom.http.resource.DynamicResourceHandler;
import io.vlingo.xoom.http.resource.RequestHandler;
import io.vlingo.xoom.http.resource.Resource;
import io.vlingo.xoom.turbo.ComponentRegistry;

public class HttpRequestHandlers<DATA> extends DynamicResourceHandler {
	private final String resourceName;
	private final Queries<DATA> queries;
	private List<RequestHandler> requestHandlers;

	@SuppressWarnings("unchecked")
	HttpRequestHandlers(final Stage stage, String resourceName) {
		super(stage.world().stage());
		this.resourceName = Objects.requireNonNull(resourceName, "aggregate must be non-null!");
		this.queries = ComponentRegistry.withType(QueryModelStateStoreProvider.class).queries;
		this.requestHandlers = new ArrayList<>();
	}
	
	public static HttpRequestHandlersBuilder builder() {
		return new HttpRequestHandlersBuilder();
	}

	@Override
	public Resource<?> routes() {		
		return resource(resourceName(), requestHandlerArray());
	}
	
	private RequestHandler[] requestHandlerArray() {
		return requestHandlers.toArray(new RequestHandler[requestHandlers.size()]);
	}
	
	private String resourceName() {
		return resourceName;
	}
	
	Queries<DATA> queries(){
		return queries;
	}
	
	void add(RequestHandler requestHandler) {
		requestHandlers.add(requestHandler);
	}
	
	Response responseOf(final Status status, final String entity) {
		return super.entityResponseOf(status, entity);
	}
	
	@Override
	protected ContentType contentType() {
		return ContentType.of("application/json", "charset=UTF-8");
	}
}
