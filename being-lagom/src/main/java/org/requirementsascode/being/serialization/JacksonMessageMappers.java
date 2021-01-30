package org.requirementsascode.being.serialization;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonMessageMappers {
  private ObjectMapper incomingMessageMapper;
  private ObjectMapper outgoingMessageMapper;
  private ObjectMapper allMessageMapper;

  public JacksonMessageMappers(List<Class<? extends Object>> incomingMessageTypes,
      List<Class<? extends Object>> outgoingMessageTypes) {
    requireNonNull(incomingMessageTypes, "incomingMessageTypes must be non-null");
    requireNonNull(outgoingMessageTypes, "outgoingMessageTypes must be non-null");

    setIncomingMessageMapper(newMapper(incomingMessageTypes));
    setOutgoingMessageMapper(newMapper(outgoingMessageTypes));
    setAllMessageMapper(newMapper(allOf(incomingMessageTypes, outgoingMessageTypes)));
  }

  private List<Class<? extends Object>> allOf(List<Class<? extends Object>> incomingMessageTypes,
      List<Class<? extends Object>> outgoingMessageTypes) {
    return Stream.concat(incomingMessageTypes.stream(), outgoingMessageTypes.stream()).collect(Collectors.toList());
  }

  private ObjectMapper newMapper(Collection<Class<? extends Object>> messageTypes) {
    ObjectMapper messageMapper = new ObjectMapper();
    LinkedHashSet<Class<? extends Object>> messageTypesSet = new LinkedHashSet<>(messageTypes);
    new MessageSerializationWithTypeProperty(messageTypesSet).configure(messageMapper);
    return messageMapper;
  }

  public ObjectMapper incomingMessageMapper() {
    return incomingMessageMapper;
  }

  public ObjectMapper outgoingMessageMapper() {
    return outgoingMessageMapper;
  }

  public ObjectMapper allMessageMapper() {
    return allMessageMapper;
  }

  private void setIncomingMessageMapper(ObjectMapper incomingMessageMapper) {
    this.incomingMessageMapper = incomingMessageMapper;
  }

  private void setOutgoingMessageMapper(ObjectMapper outgoingMessageMapper) {
    this.outgoingMessageMapper = outgoingMessageMapper;
  }

  private void setAllMessageMapper(ObjectMapper allMessageMapper) {
    this.allMessageMapper = allMessageMapper;
  }
}
