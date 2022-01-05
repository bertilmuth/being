package org.requirementsascode.being;

import java.util.Collection;

import io.vlingo.xoom.common.Completes;

@SuppressWarnings("all")
public interface Queries<DATA> {
  Completes<DATA> findById(String id);
  Completes<Collection<DATA>> findAll();
}