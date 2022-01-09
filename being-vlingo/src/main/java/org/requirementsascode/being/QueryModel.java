package org.requirementsascode.being;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;
import io.vlingo.xoom.symbio.Source;

public class QueryModel<DATA> {
	private final Map<Class<? extends Source<?>>,  Merge<DATA, ? extends Source<?>>> eventClassToMergeFunction;
	private final DATA emptyData;
	
	public static <DATA> QueryModel<DATA> startEmptyWith(DATA emptyData) {
		return new QueryModel<>(emptyData);
	}
	
	private QueryModel(DATA emptyData) {
		this.emptyData = Objects.requireNonNull(emptyData, "emptyData must be non-null!");
		this.eventClassToMergeFunction = new HashMap<>();
	}
	
	public <EVENT extends IdentifiedDomainEvent> QueryModel<DATA> andMerge(Class<EVENT> eventClass, Merge<DATA, EVENT> mergeFunction) {
		Objects.requireNonNull(eventClass, "eventClass must be non-null!");
		Objects.requireNonNull(mergeFunction, "mergeFunction must be non-null!");
		
		eventClassToMergeFunction.put(eventClass, mergeFunction);
		return this;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	<EVENT extends Source<?>> DATA mergeDataWithEvent(DATA dataToMerge, EVENT event) {		
		Class<? extends Source> eventClass = event.getClass();
		DATA mergedData;
		
		if(eventClassToMergeFunction.containsKey(eventClass)) {
			Merge<DATA, EVENT> mergeFunction = (Merge<DATA, EVENT>) eventClassToMergeFunction.get(eventClass);
			mergedData = mergeFunction.merge(dataToMerge, event);
		} else {
			// if event has not been found, return the input data
			mergedData = dataToMerge;
		}
				
		return mergedData;
	}
	
	public DATA emptyData() {
		return emptyData;
	}
	
	public Set<Class<? extends Source<?>>> eventClasses(){
		return eventClassToMergeFunction.keySet();
	}
}
