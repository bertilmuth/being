package org.requirementsascode.being.lagom.serialization;

import java.util.Optional;

import org.requirementsascode.being.lagom.JsonMessage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lightbend.lagom.javadsl.api.deser.MessageSerializer;
import com.lightbend.lagom.javadsl.api.deser.SerializationException;
import com.lightbend.lagom.javadsl.api.transport.MessageProtocol;

import akka.util.ByteString;

class JsonMessageSerializer implements MessageSerializer.NegotiatedSerializer<JsonMessage, ByteString> {
  private MessageProtocol messageProtocol; 
  private final ObjectMapper messageMapper;

  public JsonMessageSerializer(ObjectMapper messageMapper) {
    this(messageMapper, new MessageProtocol(Optional.empty(), Optional.empty(), Optional.empty()));
  }
  
  public JsonMessageSerializer(ObjectMapper messageMapper, MessageProtocol messageProtocol) {
    this.messageProtocol = messageProtocol;
    this.messageMapper = messageMapper;
  }

  @Override
  public ByteString serialize(JsonMessage s) throws SerializationException {
    try {
      ByteString byteString = ByteString.fromArray(messageMapper.writeValueAsBytes(s));
      return byteString;
    } catch (JsonProcessingException e) {
      throw new SerializationException(e);
    }
  }

  @Override
  public MessageProtocol protocol() {
    return messageProtocol;
  }
}