package com.company.jifiit.model;


import com.google.gson.*;
import java.lang.reflect.Type;

public class TimestampDeserializer implements JsonDeserializer<Long> {
    @Override
    public Long deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            return json.getAsLong(); // If it's a valid long number, return it.
        } catch (NumberFormatException e) {
            return System.currentTimeMillis(); // If "Just now" or invalid, return current timestamp.
        }
    }
}
