package org.requirementsascode.being;

import io.vlingo.xoom.common.version.SemanticVersion;

public class IdentifiedDomainEvent extends io.vlingo.xoom.lattice.model.IdentifiedDomainEvent {
	private final String id;
	
	public IdentifiedDomainEvent(final String id) {
		super(SemanticVersion.from("1.0.0").toValue());
		this.id = id;
	}

	@Override
	public String identity() {
		return id;
	}
}
