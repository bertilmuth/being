package org.requirementsascode.being.serialization;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;

public class MessageSerializationWithTypeProperty {
  private static final String TYPE_PROPERTY_NAME = "@type";
  private final Map<String, String> messageTypeSimpleNameToFullNameMap;

  public MessageSerializationWithTypeProperty(Collection<Class<? extends java.lang.Object>> messageTypes) {
    this.messageTypeSimpleNameToFullNameMap = messageTypes.stream()
        .collect(Collectors.toMap(Class::getSimpleName, Class::getName));
  }
 
  /**
   * Configures a Jackson ObjectMapper.
   * 
   * @param objectMapper the ObjectMapper to configure
   */
  public void configure(ObjectMapper objectMapper) {
    objectMapper.registerModule(new PropertiesSerializationModule());
  
    PolymorphicTypeValidator ptv = 
        BasicPolymorphicTypeValidator.builder()
          .allowIfSubType(java.lang.Object.class)
            .build();
    
    StdTypeResolverBuilder typeResolverBuilder = 
      new MessageResolverBuilder(ptv)
        .init(Id.CUSTOM, new MessageResolver(messageTypeSimpleNameToFullNameMap))
        .inclusion(As.PROPERTY)
        .typeIdVisibility(false)
        .typeProperty(TYPE_PROPERTY_NAME);

    objectMapper.setDefaultTyping(typeResolverBuilder);
  }
}
