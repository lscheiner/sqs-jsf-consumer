package br.com.scheiner.aws.console.sqs.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

import br.com.scheiner.aws.console.sqs.model.SqsExplorerMessage;
import br.com.scheiner.aws.console.sqs.model.SqsExplorerQueue;
import br.com.scheiner.aws.console.sqs.model.SqsQueueDetails;
import br.com.scheiner.aws.console.sqs.service.SqsExplorerService;
import br.com.scheiner.aws.console.web.navigation.ApplicationRoute;
import br.com.scheiner.aws.console.web.navigation.NavigationManager;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;

class SqsExplorerControllerTest {

	private final SqsExplorerService sqsExplorerService = mock(SqsExplorerService.class);
	private final NavigationManager navigationManager = mock(NavigationManager.class);
	private final SqsExplorerController controller = new SqsExplorerController(this.sqsExplorerService, this.navigationManager);

	private FacesContext facesContext;
	private MockedStatic<FacesContext> facesContextMock;

	@BeforeEach
	void setUp() {
		this.facesContext = mock(FacesContext.class);
		this.facesContextMock = mockStatic(FacesContext.class);
		this.facesContextMock.when(FacesContext::getCurrentInstance).thenReturn(this.facesContext);
	}

	@AfterEach
	void tearDown() {
		this.facesContextMock.close();
	}

	private static SqsExplorerQueue novaFila(String nome, String url) {
		return new SqsExplorerQueue(nome, url, "Standard", 0, false);
	}

	private FacesMessage ultimaMensagem() {
		var captor = ArgumentCaptor.forClass(FacesMessage.class);
		verify(this.facesContext, org.mockito.Mockito.atLeastOnce()).addMessage(isNull(), captor.capture());
		return captor.getValue();
	}

	private void semMensagensAdicionadas() {
		verify(this.facesContext, never()).addMessage(any(), any());
	}


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

	@Test
	@DisplayName("Deve retornar atributos da mensagem quando houver mensagem selecionada")
	void deve_retornar_atributos_da_mensagem_quando_houver_mensagem_selecionada() {
		var atributoMensagem = MessageAttributeValue.builder().dataType("String").stringValue("valor").build();
		var mensagem = new SqsExplorerMessage();
		mensagem.setAttributes(Map.of("chave", "valor"));
		mensagem.setMessageAttributes(Map.of("origem", atributoMensagem));
		mensagem.setBodyFormatado("corpo-formatado");

		this.controller.visualizarMensagem(mensagem);

		assertThat(this.controller.getMensagemSelecionadaAttributes()).containsEntry("chave", "valor");
		assertThat(this.controller.getMensagemSelecionadaMessageAttributes()).containsEntry("origem", atributoMensagem);
	}


	@Test
	@DisplayName("Deve carregar filas com sucesso")
	void deve_carregar_filas_com_sucesso() {
		var fila1 = novaFila("fila-b", "url-b");
		var fila2 = novaFila("fila-a", "url-a");
		when(this.sqsExplorerService.listarFilas()).thenReturn(List.of(fila1, fila2));

		this.controller.carregarFilas();

		assertThat(this.controller.getFilasFiltradas()).containsExactly(fila1, fila2);
		this.semMensagensAdicionadas();
	}

	@Test
	@DisplayName("Deve tratar erro ao carregar filas")
	void deve_tratar_erro_ao_carregar_filas() {
		when(this.sqsExplorerService.listarFilas()).thenThrow(new RuntimeException("falha"));

		this.controller.carregarFilas();

		assertThat(this.controller.getFilasFiltradas()).isEmpty();
		assertEquals(FacesMessage.SEVERITY_ERROR, this.ultimaMensagem().getSeverity());
	}


	@Test
	@DisplayName("Deve selecionar fila com sucesso")
	void deve_selecionar_fila_com_sucesso() {
		var fila = novaFila("fila-a", "url-a");
		var detalhes = new SqsQueueDetails();
		var mensagem = new SqsExplorerMessage();
		when(this.sqsExplorerService.buscarDetalhes("url-a")).thenReturn(detalhes);
		when(this.sqsExplorerService.buscarMensagens("url-a", 1)).thenReturn(List.of(mensagem));

		this.controller.selecionarFila(fila);

		assertThat(this.controller.getFilaSelecionada()).isEqualTo(fila);
		assertThat(this.controller.getDetalhesFila()).isEqualTo(detalhes);
		assertThat(this.controller.getMensagens()).containsExactly(mensagem);
		assertThat(this.controller.isPossuiFilaSelecionada()).isTrue();
		assertEquals(FacesMessage.SEVERITY_INFO, this.ultimaMensagem().getSeverity());
	}

	@Test
	@DisplayName("Deve tratar erro ao selecionar fila mantendo a fila atribuida")
	void deve_tratar_erro_ao_selecionar_fila_mantendo_a_fila_atribuida() {
		var fila = novaFila("fila-b", "url-b");
		when(this.sqsExplorerService.buscarDetalhes("url-b")).thenThrow(new RuntimeException("falha"));

		this.controller.selecionarFila(fila);

		assertThat(this.controller.getFilaSelecionada()).isEqualTo(fila);
		assertThat(this.controller.getDetalhesFila()).isNull();
		assertEquals(FacesMessage.SEVERITY_ERROR, this.ultimaMensagem().getSeverity());
	}


	@Test
	@DisplayName("Deve selecionar fila da requisicao quando existir na lista de filas")
	void deve_selecionar_fila_da_requisicao_quando_existir_na_lista_de_filas() {
		var fila = novaFila("fila-x", "url-x");
		when(this.sqsExplorerService.listarFilas()).thenReturn(List.of(fila));
		when(this.navigationManager.getRequestedResource(ApplicationRoute.SQS)).thenReturn(Optional.of("fila-x"));
		when(this.sqsExplorerService.buscarDetalhes("url-x")).thenReturn(new SqsQueueDetails());
		when(this.sqsExplorerService.buscarMensagens("url-x", 1)).thenReturn(List.of());

		this.controller.init();

		assertThat(this.controller.getFilaSelecionada()).isEqualTo(fila);
	}

	@Test
	@DisplayName("Deve ignorar selecao quando fila da requisicao nao existir na lista de filas")
	void deve_ignorar_selecao_quando_fila_da_requisicao_nao_existir_na_lista_de_filas() {
		when(this.sqsExplorerService.listarFilas()).thenReturn(List.of(novaFila("fila-y", "url-y")));
		when(this.navigationManager.getRequestedResource(ApplicationRoute.SQS)).thenReturn(Optional.of("inexistente"));

		this.controller.init();

		assertThat(this.controller.getFilaSelecionada()).isNull();
	}

	@Test
	@DisplayName("Deve ignorar selecao quando nao ha recurso solicitado na navegacao")
	void deve_ignorar_selecao_quando_nao_ha_recurso_solicitado_na_navegacao() {
		when(this.sqsExplorerService.listarFilas()).thenReturn(List.of(novaFila("fila-z", "url-z")));
		when(this.navigationManager.getRequestedResource(ApplicationRoute.SQS)).thenReturn(Optional.empty());

		this.controller.init();

		assertThat(this.controller.getFilaSelecionada()).isNull();
	}


	@Test
	@DisplayName("Deve atualizar apenas as filas quando nao houver fila selecionada")
	void deve_atualizar_apenas_as_filas_quando_nao_houver_fila_selecionada() {
		when(this.sqsExplorerService.listarFilas()).thenReturn(List.of());

		this.controller.atualizarTudo();

		verify(this.sqsExplorerService, times(1)).listarFilas();
		verify(this.sqsExplorerService, never()).buscarDetalhes(any());
		verify(this.sqsExplorerService, never()).buscarMensagens(any(), any());
	}

	@Test
	@DisplayName("Deve atualizar detalhes e mensagens com waitTime informado quando houver fila selecionada")
	void deve_atualizar_detalhes_e_mensagens_com_wait_time_informado_quando_houver_fila_selecionada() {
		var fila = novaFila("fila-a", "url-a");
		ReflectionTestUtils.setField(this.controller, "filaSelecionada", fila);
		when(this.sqsExplorerService.listarFilas()).thenReturn(List.of(fila));
		when(this.sqsExplorerService.buscarDetalhes("url-a")).thenReturn(new SqsQueueDetails());
		when(this.sqsExplorerService.buscarMensagens("url-a", 7)).thenReturn(List.of());

		this.controller.atualizarTudo(7);

		verify(this.sqsExplorerService).listarFilas();
		verify(this.sqsExplorerService).buscarDetalhes("url-a");
		verify(this.sqsExplorerService).buscarMensagens("url-a", 7);
	}


	@Test
	@DisplayName("Nao deve buscar mensagens quando nao houver fila selecionada")
	void nao_deve_buscar_mensagens_quando_nao_houver_fila_selecionada() {
		this.controller.buscarMensagens();

		assertThat(this.controller.getMensagens()).isEmpty();
		verify(this.sqsExplorerService, never()).buscarMensagens(any(), any());
	}

	@Test
	@DisplayName("Deve tratar erro ao buscar mensagens")
	void deve_tratar_erro_ao_buscar_mensagens() {
		var fila = novaFila("fila-a", "url-a");
		ReflectionTestUtils.setField(this.controller, "filaSelecionada", fila);
		when(this.sqsExplorerService.buscarMensagens("url-a", 1)).thenThrow(new RuntimeException("falha"));

		this.controller.buscarMensagens();

		assertThat(this.controller.getMensagens()).isEmpty();
		assertEquals(FacesMessage.SEVERITY_ERROR, this.ultimaMensagem().getSeverity());
	}


	@Test
	@DisplayName("Nao deve enviar mensagem quando body estiver nulo ou em branco")
	void nao_deve_enviar_mensagem_quando_body_estiver_nulo_ou_em_branco() {
		this.controller.setBodyEnvio(null);
		this.controller.enviarMensagem();
		assertEquals(FacesMessage.SEVERITY_ERROR, this.ultimaMensagem().getSeverity());

		this.controller.setBodyEnvio("   ");
		this.controller.enviarMensagem();

		verify(this.sqsExplorerService, never()).enviarMensagem(any(), any(), any());
	}

	@Test
	@DisplayName("Nao deve enviar mensagem quando body nao for um JSON valido")
	void nao_deve_enviar_mensagem_quando_body_nao_for_um_json_valido() {
		this.controller.setBodyEnvio("nao-json");
		when(this.sqsExplorerService.isJsonValido("nao-json")).thenReturn(false);

		this.controller.enviarMensagem();

		assertEquals(FacesMessage.SEVERITY_ERROR, this.ultimaMensagem().getSeverity());
		verify(this.sqsExplorerService, never()).enviarMensagem(any(), any(), any());
	}

	@Test
	@DisplayName("Nao deve enviar mensagem quando message attributes nao for um JSON valido")
	void nao_deve_enviar_mensagem_quando_message_attributes_nao_for_um_json_valido() {
		this.controller.setBodyEnvio("{}");
		this.controller.setMessageAttributesEnvio("invalido");
		when(this.sqsExplorerService.isJsonValido("{}")).thenReturn(true);
		when(this.sqsExplorerService.isJsonValido("invalido")).thenReturn(false);

		this.controller.enviarMensagem();

		assertEquals(FacesMessage.SEVERITY_ERROR, this.ultimaMensagem().getSeverity());
		verify(this.sqsExplorerService, never()).enviarMensagem(any(), any(), any());
	}

	@Test
	@DisplayName("Deve enviar mensagem com sucesso, limpar campos e atualizar tudo")
	void deve_enviar_mensagem_com_sucesso_limpar_campos_e_atualizar_tudo() {
		var fila = novaFila("fila-a", "url-a");
		ReflectionTestUtils.setField(this.controller, "filaSelecionada", fila);
		this.controller.setBodyEnvio("{}");
		this.controller.setMessageAttributesEnvio(" ");
		when(this.sqsExplorerService.isJsonValido("{}")).thenReturn(true);
		when(this.sqsExplorerService.listarFilas()).thenReturn(List.of(fila));
		when(this.sqsExplorerService.buscarDetalhes("url-a")).thenReturn(new SqsQueueDetails());
		when(this.sqsExplorerService.buscarMensagens("url-a", 5)).thenReturn(List.of());

		this.controller.enviarMensagem();

		verify(this.sqsExplorerService).enviarMensagem("url-a", "{}", " ");
		assertThat(this.controller.getBodyEnvio()).isNull();
		assertThat(this.controller.getMessageAttributesEnvio()).isNull();
		verify(this.sqsExplorerService).buscarMensagens("url-a", 5);
		assertEquals(FacesMessage.SEVERITY_INFO, this.ultimaMensagem().getSeverity());
	}

	@Test
	@DisplayName("Deve tratar erro ao enviar mensagem mantendo os campos preenchidos")
	void deve_tratar_erro_ao_enviar_mensagem_mantendo_os_campos_preenchidos() {
		var fila = novaFila("fila-a", "url-a");
		ReflectionTestUtils.setField(this.controller, "filaSelecionada", fila);
		this.controller.setBodyEnvio("{}");
		when(this.sqsExplorerService.isJsonValido("{}")).thenReturn(true);
		doThrow(new RuntimeException("falha"))
				.when(this.sqsExplorerService).enviarMensagem(eq("url-a"), eq("{}"), any());

		this.controller.enviarMensagem();

		assertThat(this.controller.getBodyEnvio()).isEqualTo("{}");
		assertEquals(FacesMessage.SEVERITY_ERROR, this.ultimaMensagem().getSeverity());
	}


	@Test
	@DisplayName("Deve selecionar mensagem para visualizacao e preencher body de replay")
	void deve_selecionar_mensagem_para_visualizacao_e_preencher_body_de_replay() {
		var mensagem = new SqsExplorerMessage();
		mensagem.setBodyFormatado("{ \"a\": 1 }");

		this.controller.visualizarMensagem(mensagem);

		assertThat(this.controller.getMensagemSelecionada()).isEqualTo(mensagem);
		assertThat(this.controller.getBodyReplay()).isEqualTo("{ \"a\": 1 }");
	}


	@Test
	@DisplayName("Nao deve reenviar mensagem quando body de replay nao for um JSON valido")
	void nao_deve_reenviar_mensagem_quando_body_de_replay_nao_for_um_json_valido() {
		this.controller.setBodyReplay(null);

		this.controller.reenviarMensagem();

		assertEquals(FacesMessage.SEVERITY_ERROR, this.ultimaMensagem().getSeverity());
		verify(this.sqsExplorerService, never()).enviarMensagem(any(), any(), any());
	}

	@Test
	@DisplayName("Deve reenviar mensagem com sucesso para a mesma fila")
	void deve_reenviar_mensagem_com_sucesso_para_a_mesma_fila() {
		var fila = novaFila("fila-a", "url-a");
		ReflectionTestUtils.setField(this.controller, "filaSelecionada", fila);
		this.controller.setBodyReplay("{}");
		when(this.sqsExplorerService.isJsonValido("{}")).thenReturn(true);
		when(this.sqsExplorerService.listarFilas()).thenReturn(List.of(fila));
		when(this.sqsExplorerService.buscarDetalhes("url-a")).thenReturn(new SqsQueueDetails());
		when(this.sqsExplorerService.buscarMensagens("url-a", 1)).thenReturn(List.of());

		this.controller.reenviarMensagem();

		verify(this.sqsExplorerService).enviarMensagem("url-a", "{}", null);
		assertEquals(FacesMessage.SEVERITY_INFO, this.ultimaMensagem().getSeverity());
	}

	@Test
	@DisplayName("Deve tratar erro ao reenviar mensagem")
	void deve_tratar_erro_ao_reenviar_mensagem() {
		var fila = novaFila("fila-a", "url-a");
		ReflectionTestUtils.setField(this.controller, "filaSelecionada", fila);
		this.controller.setBodyReplay("{}");
		when(this.sqsExplorerService.isJsonValido("{}")).thenReturn(true);
		doThrow(new RuntimeException("falha"))
				.when(this.sqsExplorerService).enviarMensagem(eq("url-a"), eq("{}"), isNull());

		this.controller.reenviarMensagem();

		assertEquals(FacesMessage.SEVERITY_ERROR, this.ultimaMensagem().getSeverity());
	}


	@Test
	@DisplayName("Nao deve reenviar para fila original quando nao houver detalhes da fila")
	void nao_deve_reenviar_para_fila_original_quando_nao_houver_detalhes_da_fila() {
		this.controller.reenviarParaFilaOriginal(new SqsExplorerMessage());

		this.semMensagensAdicionadas();
		verify(this.sqsExplorerService, never()).enviarMensagem(any(), any(), any());
	}

	@Test
	@DisplayName("Nao deve reenviar para fila original quando a fila atual nao for uma DLQ com fila original")
	void nao_deve_reenviar_para_fila_original_quando_a_fila_atual_nao_for_uma_dlq_com_fila_original() {
		ReflectionTestUtils.setField(this.controller, "detalhesFila", new SqsQueueDetails());

		this.controller.reenviarParaFilaOriginal(new SqsExplorerMessage());

		this.semMensagensAdicionadas();
		verify(this.sqsExplorerService, never()).enviarMensagem(any(), any(), any());
	}

	@Test
	@DisplayName("Deve reenviar mensagem para fila original com sucesso")
	void deve_reenviar_mensagem_para_fila_original_com_sucesso() {
		var detalhes = new SqsQueueDetails();
		detalhes.setFilaOriginalUrl("url-original");
		ReflectionTestUtils.setField(this.controller, "detalhesFila", detalhes);
		var mensagem = new SqsExplorerMessage();
		mensagem.setBody("{}");

		this.controller.reenviarParaFilaOriginal(mensagem);

		verify(this.sqsExplorerService).enviarMensagem("url-original", "{}", null);
		assertEquals(FacesMessage.SEVERITY_INFO, this.ultimaMensagem().getSeverity());
	}

	@Test
	@DisplayName("Deve tratar erro ao reenviar mensagem para fila original")
	void deve_tratar_erro_ao_reenviar_mensagem_para_fila_original() {
		var detalhes = new SqsQueueDetails();
		detalhes.setFilaOriginalUrl("url-original");
		ReflectionTestUtils.setField(this.controller, "detalhesFila", detalhes);
		var mensagem = new SqsExplorerMessage();
		mensagem.setBody("{}");
		doThrow(new RuntimeException("falha"))
				.when(this.sqsExplorerService).enviarMensagem(eq("url-original"), eq("{}"), isNull());

		this.controller.reenviarParaFilaOriginal(mensagem);

		assertEquals(FacesMessage.SEVERITY_ERROR, this.ultimaMensagem().getSeverity());
	}


	@Test
	@DisplayName("Deve excluir mensagem com sucesso e atualizar a lista de mensagens")
	void deve_excluir_mensagem_com_sucesso_e_atualizar_a_lista_de_mensagens() {
		var fila = novaFila("fila-a", "url-a");
		ReflectionTestUtils.setField(this.controller, "filaSelecionada", fila);
		var mensagem = new SqsExplorerMessage();
		mensagem.setReceiptHandle("receipt-1");
		when(this.sqsExplorerService.buscarMensagens("url-a", 1)).thenReturn(List.of());

		this.controller.excluirMensagem(mensagem);

		verify(this.sqsExplorerService).excluirMensagem("url-a", "receipt-1");
		verify(this.sqsExplorerService).buscarMensagens("url-a", 1);
		assertEquals(FacesMessage.SEVERITY_INFO, this.ultimaMensagem().getSeverity());
	}

	@Test
	@DisplayName("Deve tratar erro ao excluir mensagem")
	void deve_tratar_erro_ao_excluir_mensagem() {
		var fila = novaFila("fila-a", "url-a");
		ReflectionTestUtils.setField(this.controller, "filaSelecionada", fila);
		var mensagem = new SqsExplorerMessage();
		mensagem.setReceiptHandle("receipt-1");
		doThrow(new RuntimeException("falha")).when(this.sqsExplorerService).excluirMensagem("url-a", "receipt-1");

		this.controller.excluirMensagem(mensagem);

		verify(this.sqsExplorerService, never()).buscarMensagens(any(), anyInt());
		assertEquals(FacesMessage.SEVERITY_ERROR, this.ultimaMensagem().getSeverity());
	}


	@Test
	@DisplayName("Deve executar purge com sucesso e atualizar a lista de mensagens")
	void deve_executar_purge_com_sucesso_e_atualizar_a_lista_de_mensagens() {
		var fila = novaFila("fila-a", "url-a");
		ReflectionTestUtils.setField(this.controller, "filaSelecionada", fila);
		when(this.sqsExplorerService.buscarMensagens("url-a", 1)).thenReturn(List.of());

		this.controller.purgeFila();

		verify(this.sqsExplorerService).purge("url-a");
		verify(this.sqsExplorerService).buscarMensagens("url-a", 1);
		assertEquals(FacesMessage.SEVERITY_INFO, this.ultimaMensagem().getSeverity());
	}

	@Test
	@DisplayName("Deve tratar erro ao executar purge")
	void deve_tratar_erro_ao_executar_purge() {
		var fila = novaFila("fila-a", "url-a");
		ReflectionTestUtils.setField(this.controller, "filaSelecionada", fila);
		doThrow(new RuntimeException("falha")).when(this.sqsExplorerService).purge("url-a");

		this.controller.purgeFila();

		verify(this.sqsExplorerService, never()).buscarMensagens(any(), anyInt());
		assertEquals(FacesMessage.SEVERITY_ERROR, this.ultimaMensagem().getSeverity());
	}


	@Test
	@DisplayName("Nao deve abrir DLQ quando nao houver detalhes da fila")
	void nao_deve_abrir_dlq_quando_nao_houver_detalhes_da_fila() {
		this.controller.abrirDlq();

		verify(this.sqsExplorerService, never()).buscarQueueUrlPorNome(any());
		this.semMensagensAdicionadas();
	}

	@Test
	@DisplayName("Nao deve abrir DLQ quando a fila atual nao possuir DLQ")
	void nao_deve_abrir_dlq_quando_a_fila_atual_nao_possuir_dlq() {
		ReflectionTestUtils.setField(this.controller, "detalhesFila", new SqsQueueDetails());

		this.controller.abrirDlq();

		verify(this.sqsExplorerService, never()).buscarQueueUrlPorNome(any());
		this.semMensagensAdicionadas();
	}

	@Test
	@DisplayName("Deve selecionar DLQ existente na lista de filas")
	void deve_selecionar_dlq_existente_na_lista_de_filas() {
		var detalhes = new SqsQueueDetails();
		detalhes.setDlqNome("fila-dlq");
		ReflectionTestUtils.setField(this.controller, "detalhesFila", detalhes);
		var dlq = novaFila("fila-dlq", "url-dlq");
		ReflectionTestUtils.setField(this.controller, "filas", new java.util.ArrayList<>(List.of(dlq)));
		when(this.sqsExplorerService.buscarQueueUrlPorNome("fila-dlq")).thenReturn("url-dlq");
		when(this.sqsExplorerService.buscarDetalhes("url-dlq")).thenReturn(new SqsQueueDetails());
		when(this.sqsExplorerService.buscarMensagens("url-dlq", 1)).thenReturn(List.of());

		this.controller.abrirDlq();

		assertThat(this.controller.getFilaSelecionada()).isEqualTo(dlq);
	}

	@Test
	@DisplayName("Deve criar e selecionar DLQ quando ela nao estiver na lista de filas")
	void deve_criar_e_selecionar_dlq_quando_ela_nao_estiver_na_lista_de_filas() {
		var detalhes = new SqsQueueDetails();
		detalhes.setDlqNome("fila-dlq");
		ReflectionTestUtils.setField(this.controller, "detalhesFila", detalhes);
		ReflectionTestUtils.setField(this.controller, "filas", new java.util.ArrayList<>());
		when(this.sqsExplorerService.buscarQueueUrlPorNome("fila-dlq")).thenReturn("url-dlq-nova");
		when(this.sqsExplorerService.buscarDetalhes("url-dlq-nova")).thenReturn(new SqsQueueDetails());
		when(this.sqsExplorerService.buscarMensagens("url-dlq-nova", 1)).thenReturn(List.of());

		this.controller.abrirDlq();

		assertThat(this.controller.getFilaSelecionada()).isNotNull();
		assertThat(this.controller.getFilaSelecionada().getNome()).isEqualTo("fila-dlq");
		assertThat(this.controller.getFilaSelecionada().getUrl()).isEqualTo("url-dlq-nova");
		assertThat(this.controller.getFilaSelecionada().getTipo()).isEqualTo("Standard");
		assertThat(this.controller.getFilaSelecionada().getQuantidadeMensagens()).isZero();
		assertThat(this.controller.getFilaSelecionada().isDlq()).isTrue();
	}


	@Test
	@DisplayName("Deve retornar nulo ao baixar mensagem quando nenhuma estiver selecionada")
	void deve_retornar_nulo_ao_baixar_mensagem_quando_nenhuma_estiver_selecionada() {
		assertThat(this.controller.getDownloadMensagem()).isNull();
	}

	@Test
	@DisplayName("Deve gerar conteudo para download da mensagem selecionada")
	void deve_gerar_conteudo_para_download_da_mensagem_selecionada() throws Exception {
		var mensagem = new SqsExplorerMessage();
		mensagem.setMessageId("msg-1");
		mensagem.setBody("conteudo-simples");
		ReflectionTestUtils.setField(this.controller, "mensagemSelecionada", mensagem);

		var conteudo = this.controller.getDownloadMensagem();

		assertThat(conteudo).isNotNull();
		assertThat(conteudo.getName()).isEqualTo("msg-1.json");
		assertThat(conteudo.getContentType()).isEqualTo("application/json");

		try (var stream = conteudo.getStream().get(); var buffer = new ByteArrayOutputStream()) {
			stream.transferTo(buffer);
			assertThat(buffer.toByteArray()).isEqualTo("conteudo-simples".getBytes(StandardCharsets.UTF_8));
		}
	}


	@Test
	@DisplayName("Deve retornar todas as filas quando o filtro estiver vazio")
	void deve_retornar_todas_as_filas_quando_o_filtro_estiver_vazio() {
		var fila1 = novaFila("fila-a", "url-a");
		var fila2 = novaFila("fila-b", "url-b");
		ReflectionTestUtils.setField(this.controller, "filas", new java.util.ArrayList<>(List.of(fila1, fila2)));

		this.controller.setFiltroFila(null);
		assertThat(this.controller.getFilasFiltradas()).containsExactly(fila1, fila2);

		this.controller.setFiltroFila("  ");
		assertThat(this.controller.getFilasFiltradas()).containsExactly(fila1, fila2);
	}

	@Test
	@DisplayName("Deve filtrar filas por nome ignorando maiusculas e minusculas")
	void deve_filtrar_filas_por_nome_ignorando_maiusculas_e_minusculas() {
		var fila1 = novaFila("Fila-Pedidos", "url-a");
		var fila2 = novaFila("Fila-Pagamentos", "url-b");
		ReflectionTestUtils.setField(this.controller, "filas", new java.util.ArrayList<>(List.of(fila1, fila2)));

		this.controller.setFiltroFila("PEDIDOS");

		assertThat(this.controller.getFilasFiltradas()).containsExactly(fila1);
	}


	@Test
	@DisplayName("Deve indicar se ha ou nao fila selecionada")
	void deve_indicar_se_ha_ou_nao_fila_selecionada() {
		assertThat(this.controller.isPossuiFilaSelecionada()).isFalse();

		ReflectionTestUtils.setField(this.controller, "filaSelecionada", novaFila("fila-a", "url-a"));

		assertThat(this.controller.isPossuiFilaSelecionada()).isTrue();
	}


	@Test
	@DisplayName("Deve expor getters e setters simples do controller")
	void deve_expor_getters_e_setters_simples_do_controller() {
		this.controller.setFiltroFila("filtro");
		assertThat(this.controller.getFiltroFila()).isEqualTo("filtro");

		this.controller.setBodyEnvio("body-envio");
		assertThat(this.controller.getBodyEnvio()).isEqualTo("body-envio");

		this.controller.setMessageAttributesEnvio("attrs-envio");
		assertThat(this.controller.getMessageAttributesEnvio()).isEqualTo("attrs-envio");

		this.controller.setBodyReplay("body-replay");
		assertThat(this.controller.getBodyReplay()).isEqualTo("body-replay");

		this.controller.setIntervaloAtualizacao(10);
		assertThat(this.controller.getIntervaloAtualizacao()).isEqualTo(10);
	}
	
	@Test
	@DisplayName("Deve enviar mensagem quando message attributes contiver um JSON valido")
	void deve_enviar_mensagem_quando_message_attributes_conter_um_json_valido() {
		var fila = novaFila("fila-a", "url-a");
		ReflectionTestUtils.setField(this.controller, "filaSelecionada", fila);

		this.controller.setBodyEnvio("{}");
		this.controller.setMessageAttributesEnvio("{\"origem\":\"teste\"}");

		when(this.sqsExplorerService.isJsonValido("{}")).thenReturn(true);
		when(this.sqsExplorerService.isJsonValido("{\"origem\":\"teste\"}")).thenReturn(true);

		when(this.sqsExplorerService.listarFilas()).thenReturn(List.of(fila));
		when(this.sqsExplorerService.buscarDetalhes("url-a")).thenReturn(new SqsQueueDetails());
		when(this.sqsExplorerService.buscarMensagens("url-a", 5)).thenReturn(List.of());

		this.controller.enviarMensagem();

		verify(this.sqsExplorerService)
				.enviarMensagem("url-a", "{}", "{\"origem\":\"teste\"}");
	}
}
