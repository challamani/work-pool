package com.workpool.task.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LenientInstantDeserializerTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Instant.class, new LenientInstantDeserializer());
        objectMapper.registerModule(module);
    }

    @Test
    void deserialize_validIsoInstant_returnsInstant() throws IOException {
        String json = "\"2026-04-19T09:01:00Z\"";
        Instant result = objectMapper.readValue(json, Instant.class);
        assertEquals(Instant.parse("2026-04-19T09:01:00Z"), result);
    }

    @Test
    void deserialize_localDateTime_returnsInstantAsUtc() throws IOException {
        String json = "\"2026-04-19T09:01:00\"";
        Instant result = objectMapper.readValue(json, Instant.class);
        assertEquals(Instant.parse("2026-04-19T09:01:00Z"), result);
    }

    @Test
    void deserialize_nullValue_returnsNull() throws IOException {
        LenientInstantDeserializer deserializer = new LenientInstantDeserializer();
        JsonParser parser = mock(JsonParser.class);
        DeserializationContext context = mock(DeserializationContext.class);
        when(parser.getValueAsString()).thenReturn(null);

        Instant result = deserializer.deserialize(parser, context);

        assertNull(result);
    }

    @Test
    void deserialize_blankValue_returnsNull() throws IOException {
        LenientInstantDeserializer deserializer = new LenientInstantDeserializer();
        JsonParser parser = mock(JsonParser.class);
        DeserializationContext context = mock(DeserializationContext.class);
        when(parser.getValueAsString()).thenReturn("   ");

        Instant result = deserializer.deserialize(parser, context);

        assertNull(result);
    }

    @Test
    void deserialize_invalidFormat_throwsInvalidFormatException() {
        assertThrows(InvalidFormatException.class,
                () -> objectMapper.readValue("\"not-a-date\"", Instant.class));
    }
}
