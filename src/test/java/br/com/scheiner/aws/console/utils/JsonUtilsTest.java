package br.com.scheiner.aws.console.utils;



import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JsonUtilsTest {

	@Test
	@DisplayName("Deve formatar um JSON valido com identacao")
	void deve_formatar_json_valido_com_identacao() {
		var resultado = JsonUtils.prettyPrint("{\"nome\":\"fila\",\"ativo\":true}");

		assertThat(resultado)
				.contains(System.lineSeparator())
				.contains("\"nome\" : \"fila\"")
				.contains("\"ativo\" : true");
	}

	@Test
	@DisplayName("Deve retornar o conteudo original quando o JSON for invalido")
	void deve_retornar_conteudo_original_quando_json_for_invalido() {
		var conteudo = "{json-invalido";

		assertThat(JsonUtils.prettyPrint(conteudo)).isEqualTo(conteudo);
	}
}
