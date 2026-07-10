package br.com.scheiner.aws.console.dynamodb.util;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

class DynamoDbJsonMapperTest {

	@Test
	@DisplayName("Deve converter item DynamoDB para JSON preservando tipos suportados")
	void deve_converter_item_dynamodb_para_json_preservando_tipos_suportados() {
		var item = Map.of(
				"texto", AttributeValue.builder().s("valor").build(),
				"numero", AttributeValue.builder().n("10").build(),
				"ativo", AttributeValue.builder().bool(true).build(),
				"nulo", AttributeValue.builder().nul(true).build(),
				"strings", AttributeValue.builder().ss("a", "b").build(),
				"numeros", AttributeValue.builder().ns("1", "2").build(),
				"lista", AttributeValue.builder().l(AttributeValue.builder().s("x").build()).build(),
				"mapa", AttributeValue.builder().m(Map.of("interno", AttributeValue.builder().s("y").build())).build());

		var json = DynamoDbJsonMapper.toJson(item);

		assertThat(json)
				.contains("\"texto\":{\"S\":\"valor\"}")
				.contains("\"numero\":{\"N\":\"10\"}")
				.contains("\"ativo\":{\"BOOL\":true}")
				.contains("\"nulo\":{\"NULL\":true}")
				.contains("\"strings\":{\"SS\":[\"a\",\"b\"]}")
				.contains("\"numeros\":{\"NS\":[\"1\",\"2\"]}")
				.contains("\"lista\":{\"L\":[{\"S\":\"x\"}]}")
				.contains("\"mapa\":{\"M\":{\"interno\":{\"S\":\"y\"}}}");
	}

	@Test
	@DisplayName("Deve converter JSON para AttributeValue preservando tipos suportados")
	void deve_converter_json_para_attribute_value_preservando_tipos_suportados() throws Exception {
		var json = """
				{
				  "texto": { "S": "valor" },
				  "numero": { "N": "10" },
				  "ativo": { "BOOL": true },
				  "nulo": { "NULL": true },
				  "strings": { "SS": ["a", "b"] },
				  "numeros": { "NS": ["1", "2"] },
				  "lista": { "L": [{ "S": "x" }] },
				  "mapa": { "M": { "interno": { "S": "y" } } }
				}
				""";

		var item = DynamoDbJsonMapper.fromJson(json);

		assertThat(item.get("texto").s()).isEqualTo("valor");
		assertThat(item.get("numero").n()).isEqualTo("10");
		assertThat(item.get("ativo").bool()).isTrue();
		assertThat(item.get("nulo").nul()).isTrue();
		assertThat(item.get("strings").ss()).containsExactly("a", "b");
		assertThat(item.get("numeros").ns()).containsExactly("1", "2");
		assertThat(item.get("lista").l()).extracting(AttributeValue::s).containsExactly("x");
		assertThat(item.get("mapa").m().get("interno").s()).isEqualTo("y");
	}

	@Test
	@DisplayName("Deve rejeitar JSON com tipo DynamoDB desconhecido")
	void deve_rejeitar_json_com_tipo_dynamodb_desconhecido() {
		var excecao = assertThrows(
				IllegalArgumentException.class,
				() -> DynamoDbJsonMapper.fromJson("{\"campo\":{\"X\":\"valor\"}}"));

		assertThat(excecao.getMessage()).contains("Tipo DynamoDB");
	}

	@Test
	@DisplayName("Deve converter AttributeValue vazio para string vazia ao gerar JSON")
	void deve_converter_attribute_value_vazio_para_string_vazia_ao_gerar_json() {
		var json = DynamoDbJsonMapper.toJson(Map.of("campo", AttributeValue.builder().build()));

		assertThat(json).isEqualTo("{\"campo\":{\"S\":\"\"}}");
	}

	@Test
	@DisplayName("Deve converter valor complexo isolado para JSON")
	void deve_converter_valor_complexo_isolado_para_json() {
		var valor = AttributeValue.builder()
				.l(List.of(AttributeValue.builder().n("1").build()))
				.build();

		assertThat(DynamoDbJsonMapper.toJson(valor)).isEqualTo("{\"L\":[{\"N\":\"1\"}]}");
	}
	
	@Test
	@DisplayName("Deve lançar IllegalArgumentException quando ocorrer erro ao converter valor para JSON")
	void deve_lancar_illegal_argument_exception_quando_ocorrer_erro_ao_converter_valor_para_json() {
		
		var valor = mock(AttributeValue.class);

		when(valor.s()).thenThrow(new RuntimeException("erro"));

		var excecao = assertThrows(IllegalArgumentException.class, () -> DynamoDbJsonMapper.toJson(valor));

		assertThat(excecao).hasMessage("Erro ao converter valor para JSON.").hasCauseInstanceOf(RuntimeException.class);
	}
	
	@Test
	@DisplayName("Deve lançar IllegalArgumentException quando ocorrer erro ao converter item para JSON")
	void deve_lancar_illegal_argument_exception_quando_ocorrer_erro_ao_converter_item_para_json() {
		
		var valor = mock(AttributeValue.class);
		when(valor.s()).thenThrow(new RuntimeException("erro"));

		var item = Map.of("campo", valor);

		var excecao = assertThrows(
				IllegalArgumentException.class,
				() -> DynamoDbJsonMapper.toJson(item));

		assertThat(excecao)
				.hasMessage("Erro ao converter item para JSON.")
				.hasCauseInstanceOf(RuntimeException.class);
	}
	
}
