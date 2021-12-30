package org.requirementsascode.being;

import java.util.Collection;

import io.vlingo.xoom.common.Completes;

@SuppressWarnings("all")
public interface Queries<T> {
  Completes<T> findById(String id);
  Completes<Collection<T>> findAll();
}