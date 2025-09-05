package com.example.pipeline.flink;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.types.Row;

public class JacksonRowJsonSerializer implements SerializationSchema<Row> {
    private final ObjectMapper mapper = new ObjectMapper();
    private final String[] fieldNames;

    public JacksonRowJsonSerializer(String[] fieldNames) {
        this.fieldNames = fieldNames;
    }

    @Override
    public byte[] serialize(Row row) {
        try {
            ObjectNode json = mapper.createObjectNode();
            for (int i = 0; i < row.getArity(); i++) {
                String fieldName = i < fieldNames.length ? fieldNames[i] : "field" + i;
                Object value = row.getField(i);
                switch (value) {
                    case null -> json.putNull(fieldName);
                    case String safeValue -> json.put(fieldName, safeValue);
                    case Integer safeValue -> json.put(fieldName, safeValue);
                    case Long safeValue -> json.put(fieldName, safeValue);
                    case Double safeValue -> json.put(fieldName, safeValue);
                    case Float safeValue -> json.put(fieldName, safeValue);
                    case Boolean safeValue -> json.put(fieldName, safeValue);
                    default -> json.put(fieldName, value.toString());
                }
            }
            return mapper.writeValueAsBytes(json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize row", e);
        }
    }
}