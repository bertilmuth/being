package org.requirementsascode.being;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;
import org.requirementsascode.being.serialization.MessageSerializationWithTypeProperty;
import org.requirementsascode.being.serialization.PropertiesSerializationModule;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Value;

public class SerializationTest {
  @Test
  public void serializesEventContainer() throws JsonProcessingException {
    ObjectMapper objectMapper = mapperForContainerSerialization();

    EventContainer eventContainer = new EventContainer(new TestObjectWithOneProperty("ExpectedEvent"));
    assertSerialization(objectMapper, eventContainer);
  }
  
  @Test
  public void serializesLombokEventContainer() throws JsonProcessingException {
    ObjectMapper objectMapper = mapperForContainerSerialization();

    EventContainer eventContainer = new EventContainer(new LombokTestObject("ExpectedEvent"));
    assertSerialization(objectMapper, eventContainer);
  }
  
  @Test
  public void serializesAggregateState() throws JsonProcessingException {
    ObjectMapper objectMapper = mapperForContainerSerialization();

    AggregateState expectedState = new AggregateState(new TestObjectWithOneProperty("ExpectedMessage"));
    assertSerialization(objectMapper, expectedState);
  }
  
  @Test
  public void serializesAggregateStateWithTwoProperties() throws JsonProcessingException {
    ObjectMapper objectMapper = mapperForContainerSerialization();

    AggregateState expectedState = new AggregateState(new TestObjectWithTwoProperties("Id1", "ExpectedMessage"));
    assertSerialization(objectMapper, expectedState);
  }
  
  @Test(expected = JsonMappingException.class)
  public void doesntSerializeAggregateStateWithOnePropertyWithoutAnnotation() throws JsonProcessingException {
    ObjectMapper objectMapper = mapperForContainerSerialization();

    TestObjectWithOnePropertyWithoutAnnotation aggregateRoot = new TestObjectWithOnePropertyWithoutAnnotation("ExpectedMessage");
    AggregateState expectedState = new AggregateState(aggregateRoot);
    assertSerialization(objectMapper, expectedState);
  }
  
  @Test
  public void serializesJsonMessage() throws JsonProcessingException {
    ObjectMapper objectMapper = mapperForMessageSerializationOf(TestObjectWithOneProperty.class);

    JsonMessage expectedMessage = new JsonMessage(new TestObjectWithOneProperty("ExpectedMessage"));
    assertSerialization(objectMapper, expectedMessage);
  }
  
  @Test
  public void serializesLombokJsonMessage() throws JsonProcessingException {
    ObjectMapper objectMapper = mapperForMessageSerializationOf(LombokTestObject.class);

    JsonMessage expectedMessage = new JsonMessage(new LombokTestObject("ExpectedMessage"));
    assertSerialization(objectMapper, expectedMessage);
  }
  
  private void assertSerialization(ObjectMapper objectMapper, java.lang.Object objectUnderTest)
      throws JsonProcessingException, JsonMappingException {
    String actualJson = objectMapper.writeValueAsString(objectUnderTest);
    java.lang.Object actualObject = objectMapper.readValue(actualJson, objectUnderTest.getClass());
    assertEquals(objectUnderTest, actualObject);
  }

  private ObjectMapper mapperForMessageSerializationOf(Class<?> messageClass) {
    ObjectMapper objectMapper = new ObjectMapper();
    new MessageSerializationWithTypeProperty(Arrays.asList(messageClass)).configure(objectMapper);
    return objectMapper;
  }
  
  private ObjectMapper mapperForContainerSerialization() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new PropertiesSerializationModule());
    return objectMapper;
  }
  
  private static class TestObjectWithOneProperty{
    private final String text;
    
    @JsonCreator
    public TestObjectWithOneProperty(String text) {
      this.text = text;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((text == null) ? 0 : text.hashCode());
      return result;
    }

    @Override
    public boolean equals(java.lang.Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      TestObjectWithOneProperty other = (TestObjectWithOneProperty) obj;
      if (text == null) {
        if (other.text != null)
          return false;
      } else if (!text.equals(other.text))
        return false;
      return true;
    }
  }
  
  private static class TestObjectWithTwoProperties{
    private final String id;
    private final String text;
    
    public TestObjectWithTwoProperties(String id, String text) {
      this.id = id;
      this.text = text;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      result = prime * result + ((text == null) ? 0 : text.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      TestObjectWithTwoProperties other = (TestObjectWithTwoProperties) obj;
      if (id == null) {
        if (other.id != null)
          return false;
      } else if (!id.equals(other.id))
        return false;
      if (text == null) {
        if (other.text != null)
          return false;
      } else if (!text.equals(other.text))
        return false;
      return true;
    }
  }
  
  private static class TestObjectWithOnePropertyWithoutAnnotation{
    private String text;
    
    public TestObjectWithOnePropertyWithoutAnnotation(String text) {
      this.text = text;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((text == null) ? 0 : text.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      TestObjectWithOnePropertyWithoutAnnotation other = (TestObjectWithOnePropertyWithoutAnnotation) obj;
      if (text == null) {
        if (other.text != null)
          return false;
      } else if (!text.equals(other.text))
        return false;
      return true;
    }
  }

  @Value @Properties 
  private static class LombokTestObject{
    String text;
  }
}
