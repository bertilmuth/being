package org.requirementsascode.being.greetingdata;

import io.vlingo.xoom.common.version.SemanticVersion;
import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;

public final class GreetingCreated extends IdentifiedDomainEvent {
	public final String id;
	public final String salutation;
	public final String personName;

	public GreetingCreated(final String id, final String salutation, String personName) {
	    super(SemanticVersion.from("1.0.0").toValue());
		this.id = id;
		this.salutation = salutation;
		this.personName = personName;
	}

	@Override
	public String identity() {
		return id;
	}

	@Override
	public String toString() {
		return "GreetingCreated [id=" + id + ", salutation=" + salutation + ", personName=" + personName + "]";
	}
}
