package br.com.scheiner.sqs.console.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonUtils {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private JsonUtils() {
	}

	public static String prettyPrint(String json) {

		try {
			var object = OBJECT_MAPPER.readValue(json, Object.class);
			return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object);

		} catch (Exception exception) {
			return json;
		}
	}
}