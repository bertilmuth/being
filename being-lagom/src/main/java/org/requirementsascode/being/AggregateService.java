package org.requirementsascode.being;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.pathCall;
import static java.util.Objects.requireNonNull;

import java.util.List;

import org.requirementsascode.being.serialization.JacksonMessageMappers;
import org.requirementsascode.being.serialization.JsonMessageSerialization;
import org.requirementsascode.being.serialization.ObjectSerialization;

import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;

import akka.Done;
import akka.NotUsed;

/**
 * <p>
 * Enables you to specify the interface for an aggregate service.
 * The service will react to GET and POST requests to the {@link #address()} URL.
 * </p>
 * <p>
 * Provide a default implementation for the following methods:  {@link #address()}, {@link #uniqueName()}, {@link #commandTypes()}, {@link #outgoingMessageTypes()}.
 * </p>
 * 
 * @author b_muth
 *
 */
public interface AggregateService extends Service {
  /**
   * Don't override this method, unless you're absolutely sure what you're doing.
   * @param id the aggregate root id, as specified in the {@link #address()}
   * @return service call
   */
  ServiceCall<NotUsed, JsonMessage> httpGet(String id);

  /**
   * Don't override this method, unless you're absolutely sure what you're doing.
   * @param id the aggregate root id, as specified in the {@link #address()}
   * @return service call
   */
  ServiceCall<Object, Done> httpPost(String id);

  /**
   * Don't override this method, unless you're absolutely sure what you're doing.
   */
  @Override
  default Descriptor descriptor() {
    String name = requireNonNull(uniqueName(), "name must be non-null");
    String address = requireNonNull(address(), "address must be non-null");

    JacksonMessageMappers messageMappers = new JacksonMessageMappers(commandTypes(), outgoingMessageTypes());
    ObjectSerialization objectSerialization = new ObjectSerialization(messageMappers);
    JsonMessageSerialization jsonMessageSerialization = new JsonMessageSerialization(messageMappers);

    return named(name).withCalls(pathCall(address, this::httpGet).withResponseSerializer(jsonMessageSerialization),
        pathCall(address, this::httpPost).withRequestSerializer(objectSerialization)).withAutoAcl(true);
  }

  /**
   * Define the name of the service, uniquely identifying the service.
   * IMPORTANT NOTE: Being requires that name to be immutable over time!
   * 
   * @return the immutable service name
   */
  String uniqueName();

  /**
   * <p>
   * Define a URL where the service accepts GET and POST
   * requests. For example: 
   * </p>
   * <pre>
   *  &#64;Override
   *  default String address() {
   *    return "/api/greet/:id";
   *  }
   * </pre>
   * <p>
   * Use <code>:id</code> inside the URL to state that the service caller
   * must provide the aggregate's id at that position. For example, a GET request
   * to <code>http://localhost:9000/api/greet/Joe</code> in the dev environment
   * will be directed to the aggregate with the id <code>Joe</code>.
   * <p>
   * The content of a response to a GET request is defined by {@link AggregateBehavior#responseToGet()}.
   * The response class must be part of the {@link #outgoingMessageTypes()}.
   * </p>
   * <p>
   * Which POST requests you can send to the address is defined by the {@link #commandTypes()}.
   * </p>
   * 
   * @return the web service endpoint that accepts requests.
   */

  String address();

  /**
   * Provide the classes of commands that go into the service.
   * <p>
   * The command classes need to be serializable to JSON by the Jackson library
   * (<a>https://github.com/FasterXML/jackson</a>).
   * </p>
   * <p>
   * Commands can be sent to the service via POST requests to the {@link #address()} URL.
   * The JSON body of each POST request needs to contain an additional <code>@type</code> property
   * that contains the simple name of the command's class.
   * </p>
   * @return the command classes
   */
  List<Class<?>> commandTypes();


  /**
   * Provide the classes of messages that go out of the service.
   * Typical examples are responses to GET requests, or event messages sent to other services.
   * The message classes need to be serializable to JSON by the Jackson library
   * (<a>https://github.com/FasterXML/jackson</a>).
   * 
   * @return the classes of outgoing messages
   */
  List<Class<?>> outgoingMessageTypes();
}
