package org.requirementsascode.being.lagom.serialization;

import java.util.List;
import java.util.Optional;

import org.requirementsascode.being.lagom.JsonMessage;

import com.lightbend.lagom.javadsl.api.deser.StrictMessageSerializer;
import com.lightbend.lagom.javadsl.api.transport.MessageProtocol;
import com.lightbend.lagom.javadsl.api.transport.NotAcceptable;
import com.lightbend.lagom.javadsl.api.transport.UnsupportedMediaType;

import akka.util.ByteString;

public class JsonMessageSerialization implements StrictMessageSerializer<JsonMessage> {
  private final JacksonMessageMappers messageMappers;

  public JsonMessageSerialization(JacksonMessageMappers jacksonMessageMappers) {
    this.messageMappers = jacksonMessageMappers;
  } 

  @Override
  public NegotiatedSerializer<JsonMessage, ByteString> serializerForRequest() {
    return new JsonMessageSerializer(messageMappers.incomingMessageMapper());
  }

  @Override
  public NegotiatedDeserializer<JsonMessage, ByteString> deserializer(MessageProtocol protocol)
      throws UnsupportedMediaType {
    return new JsonMessageDeserializer(messageMappers.allMessageMapper());
  }

  @Override
  public NegotiatedSerializer<JsonMessage, ByteString> serializerForResponse(List<MessageProtocol> acceptedMessageProtocols)
      throws NotAcceptable {
    return new JsonMessageSerializer(messageMappers.outgoingMessageMapper(),
        new MessageProtocol(Optional.of("application/json"), Optional.empty(), Optional.empty()));
  }

}