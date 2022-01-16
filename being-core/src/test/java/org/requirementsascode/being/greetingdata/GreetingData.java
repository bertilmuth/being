package org.requirementsascode.being.greetingdata;

@SuppressWarnings("all")
public class GreetingData {
	public final String id;
	public final String personName;
	public final String greetingText;

	public static GreetingData from(final GreetingState state) {
		return from(state.id, state.salutation, state.personName);
	}
	
	public static GreetingData from(final String id, final String salutation, final String personName) {
		return new GreetingData(id, personName, salutation + " " + personName);
	}

	public static GreetingData empty() {
		return from("", null, null);
	}

	private GreetingData(final String id, final String personName, String greetingText) {
		this.id = id;
		this.personName = personName;
		this.greetingText = greetingText;
	}

	@Override
	public String toString() {
		return "GreetingText [id=" + id + ", personName=" + personName + ", greetingText=" + greetingText + "]";
	}
}
