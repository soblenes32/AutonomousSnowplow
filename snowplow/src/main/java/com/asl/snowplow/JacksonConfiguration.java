package com.asl.snowplow;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.asl.snowplow.util.Vector3DMixin;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class JacksonConfiguration {

	@Bean
	public ObjectMapper objectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		//mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		//mapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true);
		mapper.addMixIn(Vector3D.class, Vector3DMixin.class);
		return mapper;
	}
}