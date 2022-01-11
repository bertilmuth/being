package org.requirementsascode.being;

import java.util.Collection;

import io.vlingo.xoom.common.Completes;

public interface Queries<DATA> {
  Completes<DATA> findById(String id);
  Completes<Collection<DATA>> findAll();
}