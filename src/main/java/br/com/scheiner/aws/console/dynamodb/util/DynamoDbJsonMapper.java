package br.com.scheiner.aws.console.dynamodb.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public final class DynamoDbJsonMapper {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private DynamoDbJsonMapper() {
	}

	public static String toJson(Map<String, AttributeValue> item) {
		Map<String, Object> itemJson = new LinkedHashMap<>();
		item.forEach((chave, valor) -> itemJson.put(chave, toJsonValue(valor)));

		try {
			return OBJECT_MAPPER.writeValueAsString(itemJson);
		} catch (Exception e) {
			throw new IllegalArgumentException("Erro ao converter item para JSON.", e);
		}
	}

	public static String toJson(AttributeValue valor) {
		try {
			return OBJECT_MAPPER.writeValueAsString(toJsonValue(valor));
		} catch (Exception e) {
			throw new IllegalArgumentException("Erro ao converter valor para JSON.", e);
		}
	}

	public static Map<String, AttributeValue> fromJson(String json) throws JsonProcessingException  {
		var raiz = OBJECT_MAPPER.readTree(json);
		Map<String, AttributeValue> item = new LinkedHashMap<>();

		raiz.properties().forEach(campo -> item.put(campo.getKey(), fromJsonValue(campo.getValue())));
		return item;
	}

	private static Object toJsonValue(AttributeValue valor) {
		Map<String, Object> json = new LinkedHashMap<>();

		if (valor.s() != null) {
			json.put("S", valor.s());
		} else if (valor.n() != null) {
			json.put("N", valor.n());
		} else if (valor.bool() != null) {
			json.put("BOOL", valor.bool());
		} else if (Boolean.TRUE.equals(valor.nul())) {
			json.put("NULL", true);
		} else if (valor.hasSs()) {
			json.put("SS", valor.ss());
		} else if (valor.hasNs()) {
			json.put("NS", valor.ns());
		} else if (valor.hasL()) {
			json.put("L", valor.l().stream().map(DynamoDbJsonMapper::toJsonValue).toList());
		} else if (valor.hasM()) {
			Map<String, Object> mapa = new LinkedHashMap<>();
			valor.m().forEach((chave, item) -> mapa.put(chave, toJsonValue(item)));
			json.put("M", mapa);
		} else {
			json.put("S", "");
		}

		return json;
	}

	private static AttributeValue fromJsonValue(JsonNode node) {
		if (node.has("S")) {
			return AttributeValue.builder().s(node.get("S").asText()).build();
		}
		if (node.has("N")) {
			return AttributeValue.builder().n(node.get("N").asText()).build();
		}
		if (node.has("BOOL")) {
			return AttributeValue.builder().bool(node.get("BOOL").asBoolean()).build();
		}
		if (node.has("NULL")) {
			return AttributeValue.builder().nul(node.get("NULL").asBoolean()).build();
		}
		if (node.has("SS")) {
			return AttributeValue.builder()
					.ss(OBJECT_MAPPER.convertValue(node.get("SS"), new TypeReference<List<String>>() {
					}))
					.build();
		}
		if (node.has("NS")) {
			return AttributeValue.builder()
					.ns(OBJECT_MAPPER.convertValue(node.get("NS"), new TypeReference<List<String>>() {
					}))
					.build();
		}
		if (node.has("L")) {
			List<AttributeValue> lista = new ArrayList<>();
			node.get("L").forEach(item -> lista.add(fromJsonValue(item)));
			return AttributeValue.builder().l(lista).build();
		}
		if (node.has("M")) {
			Map<String, AttributeValue> mapa = new LinkedHashMap<>();
			node.get("M").properties().forEach(campo -> mapa.put(campo.getKey(), fromJsonValue(campo.getValue())));
			return AttributeValue.builder().m(mapa).build();
		}

		throw new IllegalArgumentException("Tipo DynamoDB inválido no JSON.");
	}
}
