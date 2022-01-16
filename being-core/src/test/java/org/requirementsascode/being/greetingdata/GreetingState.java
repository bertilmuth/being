package org.requirementsascode.being.greetingdata;

public final class GreetingState {
	public final String id;
	public final String salutation;
	public final String personName;

	public static GreetingState identifiedBy(final String id) {
		return new GreetingState(id, "", "");
	}

	public GreetingState(final String id, final String salutation, final String personName) {
		this.id = id;
		this.salutation = salutation;
		this.personName = personName;
	}

	@Override
	public String toString() {
		return "GreetingState [id=" + id + ", salutation=" + salutation + ", personName=" + personName + "]";
	}
}
