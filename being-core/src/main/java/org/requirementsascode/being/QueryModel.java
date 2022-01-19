package org.requirementsascode.being;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;
import io.vlingo.xoom.symbio.Source;

/**
 * Class used to define a specific view on an aggregate's state, that can be queried then.
 * 
 * The data for the queries is created by merging the events that have happened into the query model.
 * 
 * @author b_muth
 *
 * @param <DATA> type of data that this query model creates
 */
public class QueryModel<DATA> {
	private final Map<Class<? extends Source<?>>,  Merge<? extends Source<?>, DATA>> eventClassToMergeFunctionMap;
	private final DATA emptyData;
	
	/**
	 * Define the starting point for the query model, the empty data into which all
	 * events that happened will be merged.
	 * 
	 * @param <DATA> type of data that this query model creates
	 * @param emptyData the empty data to start with
	 * @return the current instance, for method chaining
	 */
	public static <DATA> QueryModel<DATA> startEmpty(DATA emptyData) {
		return new QueryModel<>(emptyData);
	}
	
	private QueryModel(final DATA emptyData) {
		this.emptyData = requireNonNull(emptyData, "emptyData must be non-null!");
		this.eventClassToMergeFunctionMap = new HashMap<>();
	}
	
	/**
	 * Defines for the specified class of events, how events of this class are merged into the query model.
	 * For that, you need to specify a function that consumes the event and the data before merging the event,
	 * and produces the new data (into which further events will be merged).
	 * 
	 * @param <EVENT> the type of event that is consumed
	 * @param eventClass the class of the event type
	 * @param mergeFunction the function that consumes the event and the data before merging the event,
	 * and produces the new data
	 * @return the current instance, for method chaining
	 */
	public <EVENT extends IdentifiedDomainEvent> QueryModel<DATA> mergeEventsOf(final Class<EVENT> eventClass, final Merge<EVENT,DATA> mergeFunction) {
		requireNonNull(eventClass, "eventClass must be non-null!");
		requireNonNull(mergeFunction, "mergeFunction must be non-null!");
		
		eventClassToMergeFunctionMap().put(eventClass, mergeFunction);
		return this;
	}
	
	/**
	 * Returns the starting point for the query model (the empty data into which all
	 * events that happened will be merged).
	 * 
	 * @return the empty query model data
	 */
	public DATA emptyData() {
		return emptyData;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	<EVENT extends Source<?>> DATA mergeDataWithEvent(final EVENT event, final DATA dataToMerge) {		
		Class<? extends Source> eventClass = event.getClass();
		DATA mergedData;
		
		if(eventClassToMergeFunctionMap().containsKey(eventClass)) {
			Merge<EVENT,DATA> mergeFunction = (Merge<EVENT,DATA>) eventClassToMergeFunctionMap().get(eventClass);
			mergedData = mergeFunction.merge(event,dataToMerge);
		} else {
			// if event has not been found, return the input data
			mergedData = dataToMerge;
		}
				
		return mergedData;
	}
	
	private Map<Class<? extends Source<?>>, Merge<? extends Source<?>,DATA>> eventClassToMergeFunctionMap() {
		return eventClassToMergeFunctionMap;
	}
	
	Set<Class<? extends Source<?>>> eventClasses(){
		return eventClassToMergeFunctionMap.keySet();
	}
}
