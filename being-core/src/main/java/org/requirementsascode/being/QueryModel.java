package org.requirementsascode.being;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;
import io.vlingo.xoom.symbio.Source;

public class QueryModel<DATA> {
	private final Map<Class<? extends Source<?>>,  Merge<? extends Source<?>, DATA>> eventClassToMergeFunctionMap;
	private final DATA emptyData;
	
	public static <DATA> QueryModel<DATA> startEmpty(DATA emptyData) {
		return new QueryModel<>(emptyData);
	}
	
	private QueryModel(DATA emptyData) {
		this.emptyData = Objects.requireNonNull(emptyData, "emptyData must be non-null!");
		this.eventClassToMergeFunctionMap = new HashMap<>();
	}
	
	public <EVENT extends IdentifiedDomainEvent> QueryModel<DATA> mergeEventsOf(Class<EVENT> eventClass, Merge<EVENT,DATA> mergeFunction) {
		Objects.requireNonNull(eventClass, "eventClass must be non-null!");
		Objects.requireNonNull(mergeFunction, "mergeFunction must be non-null!");
		
		eventClassToMergeFunctionMap().put(eventClass, mergeFunction);
		return this;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	<EVENT extends Source<?>> DATA mergeDataWithEvent(EVENT event, DATA dataToMerge) {		
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
	
	public DATA emptyData() {
		return emptyData;
	}
	
	private Map<Class<? extends Source<?>>, Merge<? extends Source<?>,DATA>> eventClassToMergeFunctionMap() {
		return eventClassToMergeFunctionMap;
	}
	
	public Set<Class<? extends Source<?>>> eventClasses(){
		return eventClassToMergeFunctionMap.keySet();
	}
}
