package org.requirementsascode.being;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.requirementsascode.being.QueryModel;

import io.vlingo.xoom.actors.Definition;
import io.vlingo.xoom.actors.Protocols;
import io.vlingo.xoom.actors.Stage;
import io.vlingo.xoom.lattice.model.projection.ProjectionDispatcher;
import io.vlingo.xoom.lattice.model.projection.ProjectionDispatcher.ProjectToDescription;
import io.vlingo.xoom.lattice.model.projection.TextProjectionDispatcherActor;
import io.vlingo.xoom.symbio.Source;
import io.vlingo.xoom.symbio.store.dispatch.Dispatcher;
import io.vlingo.xoom.turbo.ComponentRegistry;

@SuppressWarnings("all")
public class ProjectionDispatcherProvider {

  public final ProjectionDispatcher projectionDispatcher;
  public final Dispatcher storeDispatcher;

  public static <DATA> ProjectionDispatcherProvider using(final Stage stage, QueryModel<DATA> viewModel) {
    if (ComponentRegistry.has(ProjectionDispatcherProvider.class)) {
      return ComponentRegistry.withType(ProjectionDispatcherProvider.class);
    }
    
    final List<ProjectToDescription> descriptions =
            Arrays.asList(
                    ProjectToDescription.with(ProjectionActor.class, Optional.of(viewModel), eventClassesOf(viewModel))
                    );

    final Protocols dispatcherProtocols =
            stage.actorFor(
                    new Class<?>[] { Dispatcher.class, ProjectionDispatcher.class },
                    Definition.has(TextProjectionDispatcherActor.class, Definition.parameters(descriptions)));

    final Protocols.Two<Dispatcher, ProjectionDispatcher> dispatchers = Protocols.two(dispatcherProtocols);

    return new ProjectionDispatcherProvider(dispatchers._1, dispatchers._2);
  }

  private ProjectionDispatcherProvider(final Dispatcher storeDispatcher, final ProjectionDispatcher projectionDispatcher) {
    this.storeDispatcher = storeDispatcher;
    this.projectionDispatcher = projectionDispatcher;
    ComponentRegistry.register(getClass(), this);
  }
  
  private static <DATA> Class<? extends Source<?>>[] eventClassesOf(QueryModel<DATA> viewModel) {
	Set<Class<? extends Source<?>>> eventClasses = viewModel.eventClasses();
	Class<? extends Source<?>>[] eventClassesArray = eventClasses.toArray(new Class[eventClasses.size()]);
	return eventClassesArray;
  }
}
