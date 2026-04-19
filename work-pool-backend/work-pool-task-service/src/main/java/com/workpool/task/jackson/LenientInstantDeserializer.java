package com.workpool.task.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

/**
 * Accepts standard ISO-8601 instants and local date-time values without zone.
 * Local values are interpreted as UTC for consistent backend behavior.
 */
public class LenientInstantDeserializer extends JsonDeserializer<Instant> {

    @Override
    public Instant deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String raw = parser.getValueAsString();
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String value = raw.trim();

        try {
            return Instant.parse(value);
        } catch (DateTimeParseException ignored) {
            // Fall back to local date-time parsing below.
        }

        try {
            return LocalDateTime.parse(value).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException ex) {
            throw InvalidFormatException.from(
                    parser,
                    "Invalid instant format. Use ISO-8601 (e.g. 2026-04-19T09:01:00Z)",
                    value,
                    Instant.class
            );
        }
    }
}

