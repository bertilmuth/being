package org.requirementsascode.being.greetingdata;

import io.vlingo.xoom.common.version.SemanticVersion;
import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;

public final class SalutationChanged extends IdentifiedDomainEvent {
	public final String id;
	public final String salutation;

	public SalutationChanged(final String id, final String salutation) {
	    super(SemanticVersion.from("1.0.0").toValue());		
		this.id = id;
		this.salutation = salutation;
	}
	
	@Override
	public String identity() {
		return id;
	}

	@Override
	public String toString() {
		return "SalutationChanged [id=" + id + ", salutation=" + salutation + "]";
	}
}
