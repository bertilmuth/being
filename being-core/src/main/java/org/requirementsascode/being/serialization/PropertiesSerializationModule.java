package org.requirementsascode.being.serialization;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.PropertyAccessor.FIELD;

import com.fasterxml.jackson.core.json.PackageVersion;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

@SuppressWarnings("serial")
public class PropertiesSerializationModule extends SimpleModule
{
    public PropertiesSerializationModule() {
        super(PackageVersion.VERSION);
    }

    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);
        serializeObjectProperties(context);
        
        ObjectMapper objectMapper = context.getOwner();
        makeAllClassFieldsVisible(objectMapper);
        dontFailOnUnknownProperties(objectMapper);
    }

    private void serializeObjectProperties(SetupContext context) {
      context.insertAnnotationIntrospector(new PropertiesIntrospector(new ParameterExtractor()));
    }
    
    private void makeAllClassFieldsVisible(ObjectMapper objectMapper) {
      objectMapper.setVisibility(FIELD, ANY);
    }
    
    private void dontFailOnUnknownProperties(ObjectMapper objectMapper) {
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
