package br.com.scheiner.aws.console.sqs.controller;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.scheiner.aws.console.sqs.service.SqsExplorerService;
import br.com.scheiner.aws.console.web.navigation.NavigationManager;

class SqsExplorerControllerTest {

	private final SqsExplorerController controller = new SqsExplorerController(
			mock(SqsExplorerService.class),
			mock(NavigationManager.class));

	@Test
	@DisplayName("Deve gerar preview vazio para corpo nulo ou em branco")
	void deve_gerar_preview_vazio_para_corpo_nulo_ou_em_branco() {
		assertThat(this.controller.previewBody(null)).isEmpty();
		assertThat(this.controller.previewBody("  ")).isEmpty();
	}

	@Test
	@DisplayName("Deve normalizar espacos e limitar preview de corpo longo")
	void deve_normalizar_espacos_e_limitar_preview_de_corpo_longo() {
		var corpoLongo = "x".repeat(150);

		assertThat(this.controller.previewBody("linha\n com\t espacos")).isEqualTo("linha com espacos");
		assertThat(this.controller.previewBody(corpoLongo))
				.hasSize(143)
				.endsWith("...");
	}

	@Test
	@DisplayName("Deve indicar atualizacao automatica apenas com intervalo positivo")
	void deve_indicar_atualizacao_automatica_apenas_com_intervalo_positivo() {
		this.controller.setIntervaloAtualizacao(null);
		assertThat(this.controller.isAtualizacaoAutomaticaHabilitada()).isFalse();

		this.controller.setIntervaloAtualizacao(0);
		assertThat(this.controller.isAtualizacaoAutomaticaHabilitada()).isFalse();

		this.controller.setIntervaloAtualizacao(5);
		assertThat(this.controller.isAtualizacaoAutomaticaHabilitada()).isTrue();
	}

	@Test
	@DisplayName("Deve retornar mapas vazios quando nenhuma mensagem estiver selecionada")
	void deve_retornar_mapas_vazios_quando_nenhuma_mensagem_estiver_selecionada() {
		assertThat(this.controller.getMensagemSelecionadaAttributes()).isEmpty();
		assertThat(this.controller.getMensagemSelecionadaMessageAttributes()).isEmpty();
	}
}
