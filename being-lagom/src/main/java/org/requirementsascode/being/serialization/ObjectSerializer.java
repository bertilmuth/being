package org.requirementsascode.being.serialization;

import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lightbend.lagom.javadsl.api.deser.MessageSerializer;
import com.lightbend.lagom.javadsl.api.deser.SerializationException;
import com.lightbend.lagom.javadsl.api.transport.MessageProtocol;

import akka.util.ByteString;

class ObjectSerializer implements MessageSerializer.NegotiatedSerializer<Object, ByteString> {
  private MessageProtocol messageProtocol; 
  private final ObjectMapper messageMapper;

  public ObjectSerializer(ObjectMapper messageMapper) {
    this(messageMapper, new MessageProtocol(Optional.empty(), Optional.empty(), Optional.empty()));
  }
  
  public ObjectSerializer(ObjectMapper messageMapper, MessageProtocol messageProtocol) {
    this.messageProtocol = messageProtocol;
    this.messageMapper = messageMapper;
  }

  @Override
  public ByteString serialize(Object s) throws SerializationException {
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