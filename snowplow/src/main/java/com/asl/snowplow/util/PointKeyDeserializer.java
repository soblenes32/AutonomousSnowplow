package com.asl.snowplow.util;

import java.awt.Point;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PointKeyDeserializer extends KeyDeserializer {

	private static final ObjectMapper mapper = new ObjectMapper();
	
	@Override
	public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		return mapper.readValue(key, Point.class);
	}
}
