package com.smartbear.readyapi4j.support;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.swagger.jackson.mixin.ResponseSchemaMixin;
import io.swagger.models.Response;
import io.swagger.util.DeserializationModule;
import io.swagger.util.ReferenceSerializationConfigurer;

public class ObjectMapperFactory {
    public ObjectMapperFactory() {
    }

    protected static ObjectMapper createJson() {
        return createJson(true, true);
    }

    protected static ObjectMapper createJson(boolean includePathDeserializer, boolean includeResponseDeserializer) {
        return create((JsonFactory) null, includePathDeserializer, includeResponseDeserializer);
    }

    protected static ObjectMapper createYaml() {
        return createYaml(true, true);
    }

    protected static ObjectMapper createYaml(boolean includePathDeserializer, boolean includeResponseDeserializer) {
        return create(new YAMLFactory(), includePathDeserializer, includeResponseDeserializer);
    }

    private static ObjectMapper create(JsonFactory jsonFactory, boolean includePathDeserializer, boolean includeResponseDeserializer) {
        ObjectMapper mapper = jsonFactory == null ? new ObjectMapper() : new ObjectMapper(jsonFactory);
        Module deserializerModule = new DeserializationModule(includePathDeserializer, includeResponseDeserializer);
        mapper.registerModule(deserializerModule);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.addMixIn(Response.class, ResponseSchemaMixin.class);
        ReferenceSerializationConfigurer.serializeAsComputedRef(mapper);
        return mapper;
    }
}
