package io.github.thebesteric.framework.agile.plugins.workflow.constant;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class CustomEnumDeserializer extends JsonDeserializer<LogicOperator> {
    @Override
    public LogicOperator deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getValueAsString();
        return LogicOperator.valueOf(value.toUpperCase());
    }
}