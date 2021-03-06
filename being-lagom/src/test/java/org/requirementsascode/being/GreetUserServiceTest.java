package org.requirementsascode.being;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.defaultSetup;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.withServer;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.Test;
import org.requirementsascode.being.testservice.api.GreetUserService;
import org.requirementsascode.being.testservice.api.command.ChangeGreetingText;
import org.requirementsascode.being.testservice.api.command.FailingUpdateAggregateRoot;
import org.requirementsascode.being.testservice.api.command.IgnoredCommand;
import org.requirementsascode.being.testservice.api.command.IgnoredUpdateAggregateRoot;
import org.requirementsascode.being.testservice.api.command.PublishChangeGreetingTextList;
import org.requirementsascode.being.testservice.api.command.PublishChangeGreetingTextSet;
import org.requirementsascode.being.testservice.api.command.UnknownCommand;
import org.requirementsascode.being.testservice.api.response.GreetingResponse;

import com.lightbend.lagom.javadsl.api.deser.DeserializationException;
import com.lightbend.lagom.javadsl.api.transport.BadRequest;

import akka.Done;

public class GreetUserServiceTest {

  private static final String EXPECTED_GREETING = "How are you?";

  @Test
  public void storesPersonalizedGreeting() throws Exception {
    withServer(defaultSetup().withCassandra(), server -> {
      GreetUserService service = server.client(GreetUserService.class);
      
      GreetingResponse response = get(service, "Alice");
      assertEquals("Hello, Alice!", response.getText()); // default greeting

      postChangeGreetingText(service, "Alice", "Hi");
      GreetingResponse response2 = get(service, "Alice");
      assertEquals("Hi, Alice!", response2.getText());
      
      postPublishChangeGreetingTextList(service, "Alice", "This is superseded by second greeting", EXPECTED_GREETING);
      GreetingResponse response3 = get(service, "Alice");
      assertEquals(EXPECTED_GREETING + ", Alice!", response3.getText());
      
      postPublishChangeGreetingTextSet(service, "Clara", "This is superseded by second greeting", EXPECTED_GREETING);
      GreetingResponse response4 = get(service, "Clara");
      assertEquals(EXPECTED_GREETING + ", Clara!", response4.getText());

      GreetingResponse response5 = get(service, "Bob");
      assertEquals("Hello, Bob!", response5.getText()); // default greeting remains
      
      // The next two lines must not have an effect on Bob
      postIgnoredCommand(service, "Bob");
      postIgnoredUpdateAggregateRootCommand(service, "Bob");
      
      GreetingResponse response6 = get(service, "Bob");
      assertEquals("Hello, Bob!", response6.getText()); // default greeting remains

      
      try {
        postUnknownCommand(service, "This should throw an exception"); 
        fail();
      } catch(ExecutionException e) {
        assertEquals(DeserializationException.class, e.getCause().getClass());
      }
      
      try {
        postFailingUpdateAggregateRootCommand(service, "This should throw an exception"); 
        fail();
      } catch(Exception e) {
      }
      
      try {
        postChangeGreetingText(service, "Bob", "");
        fail();
      } catch(ExecutionException e) {
        Throwable cause = e.getCause();
        assertEquals(BadRequest.class, cause.getClass());
        return;
      }
    });
  }

  private GreetingResponse get(GreetUserService service, String id)
      throws InterruptedException, ExecutionException, TimeoutException {
    JsonMessage message = service.httpGet(id).invoke().toCompletableFuture().get(5, SECONDS);
    GreetingResponse response = (GreetingResponse)message.payload();
    return response;
  }

  private Done postChangeGreetingText(GreetUserService service, String id, String newText) throws Exception {
    ChangeGreetingText command = new ChangeGreetingText(newText);
    Done done = service.httpPost(id).invoke(command).toCompletableFuture().get(5, SECONDS);
    return done;
  }
  
  private Done postPublishChangeGreetingTextList(GreetUserService service, String id, String firstGreeting, String secondGreeting) throws Exception {
    PublishChangeGreetingTextList command = new PublishChangeGreetingTextList(firstGreeting, secondGreeting);
    Done done = service.httpPost(id).invoke(command).toCompletableFuture().get(5, SECONDS);
    return done;
  }
  
  private Done postPublishChangeGreetingTextSet(GreetUserService service, String id, String firstGreeting, String secondGreeting) throws Exception {
    PublishChangeGreetingTextSet command = new PublishChangeGreetingTextSet(firstGreeting, secondGreeting);
    Done done = service.httpPost(id).invoke(command).toCompletableFuture().get(5, SECONDS);
    return done;
  }
  
  private Done postIgnoredCommand(GreetUserService service, String id) throws Exception {
    IgnoredCommand command = new IgnoredCommand();
    Done done = service.httpPost(id).invoke(command).toCompletableFuture().get(5, SECONDS);
    return done;
  }
  
  private Done postUnknownCommand(GreetUserService service, String id) throws Exception {
    UnknownCommand command = new UnknownCommand();
    Done done = service.httpPost(id).invoke(command).toCompletableFuture().get(5, SECONDS);
    return done;
  }
  
  private Done postIgnoredUpdateAggregateRootCommand(GreetUserService service, String id) throws Exception {
	  IgnoredUpdateAggregateRoot command = new IgnoredUpdateAggregateRoot();
	    Done done = service.httpPost(id).invoke(command).toCompletableFuture().get(5, SECONDS);
	    return done;
	  }
  
  private Done postFailingUpdateAggregateRootCommand(GreetUserService service, String id) throws Exception {
    FailingUpdateAggregateRoot command = new FailingUpdateAggregateRoot();
    Done done = service.httpPost(id).invoke(command).toCompletableFuture().get(5, SECONDS);
    return done;
  }
}
