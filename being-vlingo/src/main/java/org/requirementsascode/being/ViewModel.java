package org.requirementsascode.being;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import io.vlingo.xoom.symbio.Source;

public class ViewModel<DATA> {
	private final Map<Class<? extends Source<?>>,  Merge<DATA, ? extends Source<?>>> eventClassToMergeFunction;
	private final DATA emptyData;
	
	public static <DATA> ViewModel<DATA> of(DATA emptyData) {
		return new ViewModel<>(emptyData);
	}
	
	private ViewModel(DATA emptyData) {
		this.emptyData = Objects.requireNonNull(emptyData, "emptyData must be non-null!");
		this.eventClassToMergeFunction = new HashMap<>();
	}
	
	public <T extends Source<?>> void registerMerge(Class<T> eventClass, Merge<DATA, T> mergeFunction) {
		Objects.requireNonNull(eventClass, "eventClass must be non-null!");
		Objects.requireNonNull(mergeFunction, "mergeFunction must be non-null!");
		
		eventClassToMergeFunction.put(eventClass, mergeFunction);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <EVENT extends Source<?>> DATA merge(DATA dataToMerge, EVENT event) {		
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
