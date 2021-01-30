package org.requirementsascode.being.lagom;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;

/**
 * Use <code>bindService(_ServiceInterfaceName_.class, _ServiceInterfaceNameImplementation_.class);</code>
 * to bind your service API interface to your service implementation.

 * @author b_muth
 *
 */
public abstract class AggregateModule extends AbstractModule implements ServiceGuiceSupport {
}
