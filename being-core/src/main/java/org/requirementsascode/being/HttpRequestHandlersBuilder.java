package org.requirementsascode.being;

import java.util.function.Function;
import static java.util.Objects.*;

import java.util.function.Supplier;

import io.vlingo.xoom.actors.Stage;

public class HttpRequestHandlersBuilder {
	HttpRequestHandlersBuilder() {
	}

	/**
	 * Specifies the VLINGO stage (which could be a grid) on which the actors for
	 * command and query handling are created.
	 * 
	 * @param stage the stage to be used
	 * @return the builder instance for method chaining
	 */
	public StageBuilder stage(Stage stage) {
		return new StageBuilder(stage);
	}

	public class StageBuilder {
		private final Stage stage;

		StageBuilder(Stage stage) {
			this.stage = requireNonNull(stage,"stage must be non-null!");
		}

		/**
		 * Specify a supplier that enables Being to create an aggregate behavior instance,
		 * which is used for command handling.
		 * 
		 * @param <CMD>   the base type of the command types handled by the aggregate behavior
		 * @param <STATE> the state of the aggregate
		 * @param behaviorSupplier the supplier of the aggregate behavior
		 * @return the builder instance, for method chaining
		 */
		public <CMD, STATE> AggregateBuilder<CMD, STATE> behaviorSupplier(Supplier<AggregateBehavior<CMD, STATE>> behaviorSupplier) {
			return new AggregateBuilder<>(behaviorSupplier);
		}

		public class AggregateBuilder<CMD, STATE> {
			private final Supplier<AggregateBehavior<CMD, STATE>> behaviorSupplier;

			AggregateBuilder(final Supplier<AggregateBehavior<CMD, STATE>> behaviorSupplier) {
				this.behaviorSupplier = requireNonNull(behaviorSupplier,"behaviorSupplier must be non-null!");
			}

			/**
			 * Specify a function that consumes the current state of the aggregate, and produces
			 * data to be shown to the user. This function is used by Being when creating a HTTP response
			 * to a create-request or update-request.
			 * 
			 * @param <DATA> type of data produced
			 * @param dataFromState the function that transforms state to data
			 * @return this builder instance, for method chaining
			 */
			public <DATA> RequestHandlersBuilder<DATA> queryDataFromState(final Function<STATE, DATA> dataFromState) {
				return new RequestHandlersBuilder<DATA>(dataFromState);
			}

			public class RequestHandlersBuilder<DATA> {
				private final HttpRequestHandlers<CMD, STATE, DATA> httpRequestHandlers;

				public RequestHandlersBuilder(Function<STATE, DATA> dataFromState) {
					this.httpRequestHandlers = new HttpRequestHandlers<>(stage, behaviorSupplier, dataFromState);
				}

				/**
				 * Specifies a request handler for creating an aggregate instance.
				 * The request handler will accept HTTP POST requests containing JSON that represents a create command.
				 * 
				 * @param url the URL at which POST requests are received
				 * @param createRequestClass the class of the command used for creating
				 * @return this builder instance, for method chaining
				 */
				public AggregateBuilder<CMD, STATE>.RequestHandlersBuilder<DATA> createRequest(String url, Class<? extends CMD> createRequestClass) {
					httpRequestHandlers.createRequest(url, createRequestClass);
					return this;
				}

				/**
				 * Specifies a request handler for updating an aggregate instance.
				 * The request handler will accept HTTP PATCH requests containing JSON that represents an update command.
				 * The URL must literally contain the substring {id} for representing the id part.
				 * 
				 * @param url the URL at which PATCH requests are received
				 * @param updateRequestClass the class of the command used for updating
				 * @throws IllegalArgumentException if a URL without a {id} substring has been specified
				 * @return this builder instance, for method chaining
				 */
				public AggregateBuilder<CMD, STATE>.RequestHandlersBuilder<DATA> updateRequest(String url, Class<? extends CMD> updateRequestClass) {
					httpRequestHandlers.updateRequest(url, updateRequestClass);
					return this;
				}

				/**
				 * Specifies a request handler for getting the data of a single aggregate instance 
				 * (of the aggregate provided by the behavior supplier).
				 * The request handler will accept HTTP GET requests.
				 * The URL must literally contain the substring {id} for representing the id part.
				 * 
				 * @param url the URL at which GET requests are received. Must contain an {id} substring to represent the id parameter.
				 * @throws IllegalArgumentException if a URL without a {id} substring has been specified
				 * @return this builder instance, for method chaining
				 */
				public AggregateBuilder<CMD, STATE>.RequestHandlersBuilder<DATA> findByIdRequest(String url) {
					httpRequestHandlers.findByIdRequest(url);
					return this;
				}

				/**
				 * Specifies a request handler for getting the data of all aggregate instances
				 * (of the aggregate provided by the behavior supplier).
				 * 
				 * @param url the URL at which GET requests are received.
				 * @return this builder instance, for method chaining
				 */
				public AggregateBuilder<CMD, STATE>.RequestHandlersBuilder<DATA> findAllRequest(String url) {
					httpRequestHandlers.findAllRequest(url);
					return this;
				}

				/**
				 * Builds the HTTP request handlers.
				 * 
				 * @return the request handlers
				 */
				public HttpRequestHandlers<CMD, STATE, DATA> build() {
					return httpRequestHandlers;
				}
			}
		}
	}
}
