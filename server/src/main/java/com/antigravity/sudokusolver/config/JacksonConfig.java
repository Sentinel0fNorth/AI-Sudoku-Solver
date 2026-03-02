package com.antigravity.sudokusolver.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Configures Jackson to serialize/deserialize {@code char[]} as a JSON array
 * of single-character strings instead of a single concatenated string.
 *
 * Without this, Jackson turns {@code ['5','3',' ']} into {@code "53 "}
 * which breaks Gson deserialization on the Android client.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public SimpleModule charArrayModule() {
        SimpleModule module = new SimpleModule("CharArrayModule");
        module.addSerializer(char[].class, new CharArraySerializer());
        module.addDeserializer(char[].class, new CharArrayDeserializer());
        return module;
    }

    private static class CharArraySerializer extends JsonSerializer<char[]> {
        @Override
        public void serialize(char[] value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            gen.writeStartArray();
            for (char c : value) {
                gen.writeString(String.valueOf(c));
            }
            gen.writeEndArray();
        }
    }

    private static class CharArrayDeserializer extends JsonDeserializer<char[]> {
        @Override
        public char[] deserialize(JsonParser p, DeserializationContext ctxt)
                throws IOException {
            ArrayNode arrayNode = p.readValueAsTree();
            char[] result = new char[arrayNode.size()];
            for (int i = 0; i < arrayNode.size(); i++) {
                String val = arrayNode.get(i).asText();
                result[i] = val.isEmpty() ? ' ' : val.charAt(0);
            }
            return result;
        }
    }
}
