package org.requirementsascode.being;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.requirementsascode.being.greetingdata.Bootstrap.CREATE_PATH;
import static org.requirementsascode.being.greetingdata.Bootstrap.FIND_ALL_PATH;
import static org.requirementsascode.being.greetingdata.Bootstrap.FIND_BY_ID_PATH;
import static org.requirementsascode.being.greetingdata.Bootstrap.UPDATE_PATH;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.requirementsascode.being.greetingdata.Bootstrap;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

class HttpTest {
	private static final int PORT = 8081;
	private static final String DEFAULT_SALUTATION = "Hello,";

	@BeforeEach
	void setup() throws Exception {
		Bootstrap.main(new String[] {});
	}

	@Test
	void checkGreetingsCreationAndUpdate() {
		assertNoGreetingsCreated();
		
		final String jillsId = createAndAssertGreeting("Jill", DEFAULT_SALUTATION);
		final String joesId = createAndAssertGreeting("Joe", DEFAULT_SALUTATION);
		
		greetingsContain("id", jillsId, joesId);
		greetingsContain("personName", "Jill", "Joe");
		greetingsContain("greetingText", DEFAULT_SALUTATION + " Jill", DEFAULT_SALUTATION + " Joe");
		
		updateAndAssertGreeting(joesId, "Joe", "Hi,");
		updateAndAssertGreeting(jillsId, "Jill", "Howdy");
		
		greetingsContain("id", jillsId, joesId);
		greetingsContain("personName", "Jill", "Joe");
		greetingsContain("greetingText", "Howdy Jill", "Hi, Joe");
	}

	private void assertNoGreetingsCreated() {
		givenJsonClient()
			.get(FIND_ALL_PATH)
			.then()
			.body("$", hasSize(0));
	}
	
	private void greetingsContain(final String propertyName, final String... values) {
		givenJsonClient()
			.get(FIND_ALL_PATH)
			.then()
			.body(propertyName, hasItems(values));
	}

	private String createAndAssertGreeting(final String personName, final String salutation) {
		Response greetingData = 
			givenJsonClient()
				.body("{\"personName\":\"" + personName + "\"}")
				.post(CREATE_PATH);
		
		final String expectedGreetingText = salutation + " " + personName;

		assertThat(json(greetingData, "personName"), is(personName));
		assertThat(json(greetingData, "greetingText"), is(expectedGreetingText));
		
		final String greetingId = json(greetingData, "id");
		
		assertGet(greetingId, personName, expectedGreetingText);
	
		return greetingId;
	}

	private void updateAndAssertGreeting(final String greetingId, final String personName, final String salutation) {
		Response greetingData = 
			givenJsonClient()
				.body("{\"salutation\":\"" + salutation + "\"}")
				.patch(pathWithId(UPDATE_PATH, greetingId));
		
		final String expectedGreetingText = salutation + " " + personName;

		assertThat(json(greetingData, "personName"), is(personName));
		assertThat(json(greetingData, "greetingText"), is(expectedGreetingText));
		
		assertGet(greetingId, personName, expectedGreetingText);
	}
	
	private void assertGet(final String greetingId, final String personName, final String expectedGreetingText) {
		givenJsonClient()
			.get(pathWithId(FIND_BY_ID_PATH, greetingId))
			.then()
			.body("id", is(greetingId))
			.body("personName", is(personName))
			.body("greetingText", is(expectedGreetingText));
	}

	private String pathWithId(final String path, final String greetingId) {
		return path.replace("{id}", greetingId);
	}

	private String json(Response response, String path) {
		return response.jsonPath().get(path);
	}

	protected RequestSpecification givenJsonClient() {
		return given()
			.port(PORT)
			.accept(ContentType.JSON)
			.contentType(ContentType.JSON);
	}
}
