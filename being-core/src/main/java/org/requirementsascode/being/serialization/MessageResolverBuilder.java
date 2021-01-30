package org.requirementsascode.being.serialization;

import org.requirementsascode.being.Properties;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTypeResolverBuilder;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;

class MessageResolverBuilder extends DefaultTypeResolverBuilder {
  private static final long serialVersionUID = 1L;

  public MessageResolverBuilder(PolymorphicTypeValidator ptv) {
    super(DefaultTyping.NON_FINAL, ptv);
  }

  @Override
  public boolean useForType(JavaType t) {
    boolean useForCurrentClass = t.isJavaLangObject() || t.getRawClass().isAnnotationPresent(Properties.class);
    return useForCurrentClass;
  }
}