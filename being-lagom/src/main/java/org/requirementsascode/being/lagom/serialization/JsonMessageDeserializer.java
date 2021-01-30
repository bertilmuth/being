package org.requirementsascode.being.lagom.serialization;

import java.io.IOException;

import org.requirementsascode.being.lagom.JsonMessage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lightbend.lagom.javadsl.api.deser.DeserializationException;
import com.lightbend.lagom.javadsl.api.deser.MessageSerializer;

import akka.util.ByteString;

class JsonMessageDeserializer implements MessageSerializer.NegotiatedDeserializer<JsonMessage, ByteString> {
  private final ObjectMapper messageMapper;

  public JsonMessageDeserializer(ObjectMapper messageMapper) {
    this.messageMapper = messageMapper;
  }

  @Override
  public JsonMessage deserialize(ByteString bytes) throws DeserializationException {
    try {
      return messageMapper.readValue(bytes.iterator().asInputStream(), JsonMessage.class);
    } catch (IOException e) {
      throw new DeserializationException(e);
    }
  }
}