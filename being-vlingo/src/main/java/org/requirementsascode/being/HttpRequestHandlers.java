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

	HttpRequestHandlers(final Stage stage, String resourceName, Class<? extends Object> dataTypeOfAggregate) {
		super(stage.world().stage());
		this.resourceName = Objects.requireNonNull(resourceName, "resourceName must be non-null!");
		this.queries = queriesByDataType(dataTypeOfAggregate);
		this.requestHandlers = new ArrayList<>();
	}

	@SuppressWarnings("unchecked")
	private Queries<DATA> queriesByDataType(Class<? extends Object> dataType) {
		return (Queries<DATA>) ComponentRegistry.withType(QueryModelStateStoreProvider.class).queriesByDataType.get(dataType);
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
