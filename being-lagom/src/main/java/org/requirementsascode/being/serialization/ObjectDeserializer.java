package org.requirementsascode.being.serialization;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lightbend.lagom.javadsl.api.deser.DeserializationException;
import com.lightbend.lagom.javadsl.api.deser.MessageSerializer;

import akka.util.ByteString;

class ObjectDeserializer implements MessageSerializer.NegotiatedDeserializer<Object, ByteString> {
  private final ObjectMapper messageMapper;

  public ObjectDeserializer(ObjectMapper messageMapper) {
    this.messageMapper = messageMapper;
  }

  @Override
  public Object deserialize(ByteString bytes) throws DeserializationException {
    try {
      return messageMapper.readValue(bytes.iterator().asInputStream(), Object.class);
    } catch (IOException e) {
      throw new DeserializationException(e);
    }
  }
}