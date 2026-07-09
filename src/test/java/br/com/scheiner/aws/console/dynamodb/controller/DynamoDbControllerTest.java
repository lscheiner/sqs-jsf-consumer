package br.com.scheiner.aws.console.dynamodb.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.scheiner.aws.console.dynamodb.service.DynamoDbService;
import br.com.scheiner.aws.console.web.navigation.NavigationManager;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

class DynamoDbControllerTest {

	private final DynamoDbController controller = new DynamoDbController(
			mock(DynamoDbService.class),
			mock(NavigationManager.class));

	@Test
	@DisplayName("Deve formatar valores simples de AttributeValue")
	void deve_formatar_valores_simples_de_attribute_value() {
		assertThat(this.controller.formatarValor(AttributeValue.builder().s("texto").build())).isEqualTo("texto");
		assertThat(this.controller.formatarValor(AttributeValue.builder().n("10.5").build())).isEqualTo("10.5");
		assertThat(this.controller.formatarValor(AttributeValue.builder().bool(true).build())).isEqualTo("true");
		assertThat(this.controller.formatarValor(AttributeValue.builder().nul(true).build())).isEmpty();
		assertThat(this.controller.formatarValor(null)).isEmpty();
	}

	@Test
	@DisplayName("Deve manter numero como texto quando nao for numerico")
	void deve_manter_numero_como_texto_quando_nao_for_numerico() {
		assertThat(this.controller.obterValorComparable(AttributeValue.builder().n("NaN-local").build()))
				.isEqualTo("NaN-local");
	}

	@Test
	@DisplayName("Deve identificar valores complexos")
	void deve_identificar_valores_complexos() {
		assertThat(this.controller.isValorComplexo(AttributeValue.builder().m(Map.of("a", AttributeValue.builder().s("b").build())).build())).isTrue();
		assertThat(this.controller.isValorComplexo(AttributeValue.builder().l(List.of(AttributeValue.builder().s("a").build())).build())).isTrue();
		assertThat(this.controller.isValorComplexo(AttributeValue.builder().ss("a").build())).isTrue();
		assertThat(this.controller.isValorComplexo(AttributeValue.builder().ns("1").build())).isTrue();
		assertThat(this.controller.isValorComplexo(AttributeValue.builder().b(SdkBytes.fromUtf8String("bin")).build())).isTrue();
		assertThat(this.controller.isValorComplexo(AttributeValue.builder().bs(SdkBytes.fromUtf8String("bin")).build())).isTrue();
		assertThat(this.controller.isValorComplexo(AttributeValue.builder().s("simples").build())).isFalse();
		assertThat(this.controller.isValorComplexo(null)).isFalse();
	}

	@Test
	@DisplayName("Deve gerar preview para valores complexos")
	void deve_gerar_preview_para_valores_complexos() {
		assertThat(this.controller.formatarValor(AttributeValue.builder().m(Map.of("a", AttributeValue.builder().s("b").build())).build()))
				.isEqualTo("{1 campos}");
		assertThat(this.controller.formatarValor(AttributeValue.builder().l(List.of(AttributeValue.builder().s("a").build())).build()))
				.isEqualTo("[1 itens]");
		assertThat(this.controller.formatarValor(AttributeValue.builder().ss("a", "b").build()))
				.isEqualTo("[2 strings]");
		assertThat(this.controller.formatarValor(AttributeValue.builder().ns("1", "2").build()))
				.isEqualTo("[2 números]");
		assertThat(this.controller.formatarValor(AttributeValue.builder().b(SdkBytes.fromUtf8String("bin")).build()))
				.isEqualTo("[binário]");
		assertThat(this.controller.formatarValor(AttributeValue.builder().bs(SdkBytes.fromUtf8String("a"), SdkBytes.fromUtf8String("b")).build()))
				.isEqualTo("[2 binários]");
	}

	@Test
	@DisplayName("Deve expor nulos quando nenhuma tabela estiver selecionada")
	void deve_expor_nulos_quando_nenhuma_tabela_estiver_selecionada() {
		assertThat(this.controller.getPartitionKey()).isNull();
		assertThat(this.controller.getSortKey()).isNull();
		assertThat(this.controller.getTipoPartitionKey()).isNull();
		assertThat(this.controller.getTipoSortKey()).isNull();
		assertThat(this.controller.getQuantidadeItens()).isNull();
		assertThat(this.controller.getStatusTabela()).isNull();
	}
}
