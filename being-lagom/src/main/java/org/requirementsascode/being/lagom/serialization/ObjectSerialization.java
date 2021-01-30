package org.requirementsascode.being.lagom.serialization;

import java.util.List;

import com.lightbend.lagom.javadsl.api.deser.StrictMessageSerializer;
import com.lightbend.lagom.javadsl.api.transport.MessageProtocol;
import com.lightbend.lagom.javadsl.api.transport.NotAcceptable;
import com.lightbend.lagom.javadsl.api.transport.UnsupportedMediaType;

import akka.util.ByteString;

public class ObjectSerialization implements StrictMessageSerializer<Object> {
  private JacksonMessageMappers messageMappers;

  public ObjectSerialization(JacksonMessageMappers jacksonMessageMappers) {
    this.messageMappers = jacksonMessageMappers;
  } 

  @Override
  public NegotiatedSerializer<Object, ByteString> serializerForRequest() {
    return new ObjectSerializer(messageMappers.incomingMessageMapper());
  }

  @Override
  public NegotiatedDeserializer<Object, ByteString> deserializer(MessageProtocol protocol)
      throws UnsupportedMediaType {
    return new ObjectDeserializer(messageMappers.allMessageMapper());
  }

  @Override
  public NegotiatedSerializer<Object, ByteString> serializerForResponse(List<MessageProtocol> acceptedMessageProtocols)
      throws NotAcceptable {
    return new ObjectSerializer(messageMappers.outgoingMessageMapper());
  }
}