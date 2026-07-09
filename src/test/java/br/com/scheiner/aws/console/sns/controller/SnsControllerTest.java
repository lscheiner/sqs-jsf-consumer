package br.com.scheiner.aws.console.sns.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import br.com.scheiner.aws.console.sns.model.SnsSubscription;
import br.com.scheiner.aws.console.sns.model.SnsTopic;
import br.com.scheiner.aws.console.sns.service.SnsService;
import br.com.scheiner.aws.console.web.navigation.ApplicationRoute;
import br.com.scheiner.aws.console.web.navigation.NavigationManager;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

class SnsControllerTest {

	private SnsService snsService;

	private NavigationManager navigationManager;

	private SnsController controller;

	@BeforeEach
	void setup() {

		snsService = mock(SnsService.class);

		navigationManager = mock(NavigationManager.class);

		controller = new SnsController(snsService, navigationManager);
	}

	private SnsTopic criar_topico() {

		return new SnsTopic("topico-1", "arn:aws:sns:::topico-1", 2);
	}

	private SnsSubscription criar_assinatura() {

		return new SnsSubscription("arn:subscription", "sqs", "fila");
	}

	@Test
	@DisplayName("Deve inicializar controller sem tópico na requisição")
	void deve_inicializar_controller_sem_topico_na_requisicao() {

		when(snsService.listarTopicos()).thenReturn(List.of(criar_topico()));

		when(navigationManager.getRequestedResource(ApplicationRoute.SNS)).thenReturn(Optional.empty());

		controller.init();

		assertThat(controller.getTopicosFiltrados()).hasSize(1);

		assertThat(controller.getTopicoSelecionado()).isNull();

		verify(navigationManager).getRequestedResource(ApplicationRoute.SNS);
	}

	@Test
	@DisplayName("Deve inicializar controller selecionando tópico da requisição")
	void deve_inicializar_controller_selecionando_topico_da_requisicao() {

		var topico = criar_topico();

		when(snsService.listarTopicos()).thenReturn(List.of(topico));

		when(snsService.listarAssinaturas(topico.getArn())).thenReturn(List.of(criar_assinatura()));

		when(navigationManager.getRequestedResource(ApplicationRoute.SNS)).thenReturn(Optional.of(topico.getArn()));

		var facesContext = mock(FacesContext.class);

		try (var mocked = mockStatic(FacesContext.class)) {

			mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			controller.init();

			assertThat(controller.getTopicoSelecionado()).isSameAs(topico);

			assertThat(controller.getAssinaturas()).hasSize(1);

			verify(facesContext).addMessage(isNull(), any(FacesMessage.class));
		}
	}

	@Test
	@DisplayName("Deve carregar tópicos")
	void deve_carregar_topicos() {

		var topico = criar_topico();

		when(snsService.listarTopicos()).thenReturn(List.of(topico));

		controller.carregarTopicos();

		assertThat(controller.getTopicosFiltrados()).containsExactly(topico);
	}

	@Test
	@DisplayName("Deve tratar erro ao carregar tópicos")
	void deve_tratar_erro_ao_carregar_topicos() {

		when(snsService.listarTopicos()).thenThrow(new RuntimeException());

		var facesContext = mock(FacesContext.class);

		try (var mocked = mockStatic(FacesContext.class)) {

			mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			controller.carregarTopicos();

			assertThat(controller.getTopicosFiltrados()).isEmpty();

			verify(facesContext).addMessage(isNull(), any(FacesMessage.class));
		}
	}

	@Test
	@DisplayName("Deve selecionar tópico")
	void deve_selecionar_topico() {

		var topico = criar_topico();
		var assinatura = criar_assinatura();

		when(snsService.listarAssinaturas(topico.getArn())).thenReturn(List.of(assinatura));

		var facesContext = mock(FacesContext.class);

		try (var mocked = mockStatic(FacesContext.class)) {

			mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			controller.selecionarTopico(topico);

			assertThat(controller.getTopicoSelecionado()).isSameAs(topico);

			assertThat(controller.getAssinaturas()).containsExactly(assinatura);

			verify(facesContext).addMessage(isNull(), any(FacesMessage.class));
		}
	}

	@Test
	@DisplayName("Deve tratar erro ao selecionar tópico")
	void deve_tratar_erro_ao_selecionar_topico() {

		var topico = criar_topico();

		when(snsService.listarAssinaturas(topico.getArn())).thenThrow(new RuntimeException());

		var facesContext = mock(FacesContext.class);

		try (var mocked = mockStatic(FacesContext.class)) {

			mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			controller.selecionarTopico(topico);

			assertThat(controller.getTopicoSelecionado()).isSameAs(topico);

			verify(facesContext).addMessage(isNull(), any(FacesMessage.class));
		}
	}

	@Test
	@DisplayName("Deve atualizar todos os dados quando houver tópico selecionado")
	void deve_atualizar_todos_os_dados_quando_houver_topico_selecionado() {

		var topico = criar_topico();
		var assinatura = criar_assinatura();

		when(snsService.listarTopicos()).thenReturn(List.of(topico));

		when(snsService.listarAssinaturas(topico.getArn())).thenReturn(List.of(assinatura));

		var facesContext = mock(FacesContext.class);

		try (var mockedFacesContext = mockStatic(FacesContext.class)) {

			mockedFacesContext.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			controller.selecionarTopico(topico);

			controller.atualizarTudo();

			assertThat(controller.getTopicosFiltrados()).containsExactly(topico);

			assertThat(controller.getTopicoSelecionado()).isSameAs(topico);

			assertThat(controller.getAssinaturas()).containsExactly(assinatura);
		}
	}

	@Test
	@DisplayName("Deve atualizar tópicos quando não houver tópico selecionado")
	void deve_atualizar_topicos_quando_nao_houver_topico_selecionado() {

		var topico = criar_topico();

		when(snsService.listarTopicos()).thenReturn(List.of(topico));

		controller.atualizarTudo();

		assertThat(controller.getTopicosFiltrados()).containsExactly(topico);

		assertThat(controller.getTopicoSelecionado()).isNull();
	}

	@Test
	@DisplayName("Não deve selecionar tópico inexistente informado na requisição")
	void nao_deve_selecionar_topico_inexistente_informado_na_requisicao() {

		when(snsService.listarTopicos()).thenReturn(List.of(criar_topico()));

		when(navigationManager.getRequestedResource(ApplicationRoute.SNS))
				.thenReturn(Optional.of("topico-inexistente"));

		controller.init();

		assertThat(controller.getTopicoSelecionado()).isNull();

		assertThat(controller.getAssinaturas()).isEmpty();
	}

	@Test
	@DisplayName("Não deve publicar mensagem quando nenhum tópico estiver selecionado")
	void nao_deve_publicar_mensagem_quando_nenhum_topico_estiver_selecionado() {

		var facesContext = mock(FacesContext.class);

		try (var mockedFacesContext = mockStatic(FacesContext.class)) {

			mockedFacesContext.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			controller.publicarMensagem();

			verifyNoInteractions(snsService);

			verify(facesContext).addMessage(isNull(), any(FacesMessage.class));
		}
	}

	@Test
	@DisplayName("Não deve publicar mensagem quando o JSON do corpo for inválido")
	void nao_deve_publicar_mensagem_quando_json_do_corpo_for_invalido() {

		var topico = criar_topico();

		when(snsService.listarAssinaturas(topico.getArn())).thenReturn(List.of());

		when(snsService.isJsonValido("invalido")).thenReturn(false);

		var facesContext = mock(FacesContext.class);

		try (var mockedFacesContext = mockStatic(FacesContext.class)) {

			mockedFacesContext.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			controller.selecionarTopico(topico);
			controller.setBodyEnvio("invalido");

			controller.publicarMensagem();

			verify(snsService).isJsonValido("invalido");

			verify(facesContext, atLeastOnce()).addMessage(isNull(), any(FacesMessage.class));
		}
	}

	@Test
	@DisplayName("Não deve publicar mensagem quando corpo estiver vazio")
	void nao_deve_publicar_mensagem_quando_corpo_estiver_vazio() {

		when(snsService.listarAssinaturas(any())).thenReturn(List.of());

		var topico = new SnsTopic("Topico", "arn:teste", 0);

		var facesContext = mock(FacesContext.class);

		try (var mocked = mockStatic(FacesContext.class)) {

			mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			controller.selecionarTopico(topico);

			controller.setBodyEnvio("");

			controller.publicarMensagem();

			verify(snsService, never()).publicarMensagem(any(), any(), any(), any());

			verify(facesContext, atLeastOnce()).addMessage(isNull(), any(FacesMessage.class));
		}
	}

	@Test
	@DisplayName("Não deve publicar mensagem quando corpo não for JSON válido")
	void nao_deve_publicar_mensagem_quando_corpo_for_json_invalido() {

		when(snsService.listarAssinaturas(any())).thenReturn(List.of());

		when(snsService.isJsonValido("{teste")).thenReturn(false);

		var topico = new SnsTopic("Topico", "arn:teste", 0);

		var facesContext = mock(FacesContext.class);

		try (var mocked = mockStatic(FacesContext.class)) {

			mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			controller.selecionarTopico(topico);

			controller.setBodyEnvio("{teste");

			controller.publicarMensagem();

			verify(snsService, never()).publicarMensagem(any(), any(), any(), any());

			verify(facesContext, atLeastOnce()).addMessage(isNull(), any(FacesMessage.class));
		}
	}

	@Test
	@DisplayName("Não deve publicar mensagem quando atributos forem JSON inválido")
	void nao_deve_publicar_mensagem_quando_atributos_forem_json_invalido() {

		when(snsService.listarAssinaturas(any())).thenReturn(List.of());

		when(snsService.isJsonValido("{}")).thenReturn(true);

		when(snsService.isJsonValido("{erro")).thenReturn(false);

		var topico = new SnsTopic("Topico", "arn:teste", 0);

		var facesContext = mock(FacesContext.class);

		try (var mocked = mockStatic(FacesContext.class)) {

			mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			controller.selecionarTopico(topico);

			controller.setBodyEnvio("{}");
			controller.setMessageAttributesEnvio("{erro");

			controller.publicarMensagem();

			verify(snsService, never()).publicarMensagem(any(), any(), any(), any());

			verify(facesContext, atLeastOnce()).addMessage(isNull(), any(FacesMessage.class));
		}
	}

	@Test
	@DisplayName("Deve publicar mensagem com sucesso")
	void deve_publicar_mensagem_com_sucesso() {

		when(snsService.listarAssinaturas(any())).thenReturn(List.of());

		when(snsService.isJsonValido(any())).thenReturn(true);

		var topico = new SnsTopic("Topico", "arn:teste", 0);

		var facesContext = mock(FacesContext.class);

		try (var mocked = mockStatic(FacesContext.class)) {

			mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			controller.selecionarTopico(topico);

			controller.setSubjectEnvio("subject");
			controller.setBodyEnvio("{}");
			controller.setMessageAttributesEnvio("{}");

			controller.publicarMensagem();

			verify(snsService).publicarMensagem("arn:teste", "subject", "{}", "{}");

			assertThat(controller.getSubjectEnvio()).isNull();
			assertThat(controller.getBodyEnvio()).isNull();
			assertThat(controller.getMessageAttributesEnvio()).isNull();

			verify(facesContext, atLeastOnce()).addMessage(isNull(), any(FacesMessage.class));
		}
	}

	@Test
	@DisplayName("Deve tratar erro ao publicar mensagem")
	void deve_tratar_erro_ao_publicar_mensagem() {

		when(snsService.listarAssinaturas(any())).thenReturn(List.of());

		when(snsService.isJsonValido(any())).thenReturn(true);

		doThrow(new RuntimeException("erro")).when(snsService).publicarMensagem(any(), any(), any(), any());

		var topico = new SnsTopic("Topico", "arn:teste", 0);

		var facesContext = mock(FacesContext.class);

		try (var mocked = mockStatic(FacesContext.class)) {

			mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			controller.selecionarTopico(topico);

			controller.setBodyEnvio("{}");

			controller.publicarMensagem();

			verify(snsService).publicarMensagem(any(), any(), any(), any());

			verify(facesContext, atLeastOnce()).addMessage(isNull(), any(FacesMessage.class));
		}
	}

	@Test
	@DisplayName("Deve retornar todos os tópicos quando filtro for nulo")
	void deve_retornar_todos_os_topicos_quando_filtro_for_nulo() {

		when(snsService.listarTopicos()).thenReturn(List.of(criar_topico(), new SnsTopic("topico-2", "arn2", 0)));

		controller.carregarTopicos();

		controller.setFiltroTopico(null);

		assertThat(controller.getTopicosFiltrados()).hasSize(2);
	}

	@Test
	@DisplayName("Deve retornar todos os tópicos quando filtro estiver em branco")
	void deve_retornar_todos_os_topicos_quando_filtro_estiver_em_branco() {

		when(snsService.listarTopicos()).thenReturn(List.of(criar_topico(), new SnsTopic("topico-2", "arn2", 0)));

		controller.carregarTopicos();

		controller.setFiltroTopico("   ");

		assertThat(controller.getTopicosFiltrados()).hasSize(2);
	}

	@Test
	@DisplayName("Deve filtrar tópicos pelo nome")
	void deve_filtrar_topicos_pelo_nome() {

		var t1 = criar_topico();
		var t2 = new SnsTopic("pedido", "arn:pedido", 0);

		when(snsService.listarTopicos()).thenReturn(List.of(t1, t2));

		controller.carregarTopicos();

		controller.setFiltroTopico("pedido");

		assertThat(controller.getTopicosFiltrados()).containsExactly(t2);
	}

	@Test
	@DisplayName("Deve filtrar tópicos pelo ARN")
	void deve_filtrar_topicos_pelo_arn() {

		var t1 = criar_topico();
		var t2 = new SnsTopic("pedido", "arn:especial", 0);

		when(snsService.listarTopicos()).thenReturn(List.of(t1, t2));

		controller.carregarTopicos();

		controller.setFiltroTopico("especial");

		assertThat(controller.getTopicosFiltrados()).containsExactly(t2);
	}

	@Test
	@DisplayName("Deve retornar filtro informado")
	void deve_retornar_filtro_informado() {

		controller.setFiltroTopico("abc");

		assertThat(controller.getFiltroTopico()).isEqualTo("abc");
	}

	@Test
	@DisplayName("Deve publicar mensagem quando atributos estiverem em branco")
	void deve_publicar_mensagem_quando_atributos_estiverem_em_branco() {

		when(snsService.listarAssinaturas(any())).thenReturn(List.of());
		when(snsService.isJsonValido("{}")).thenReturn(true);

		var topico = new SnsTopic("Topico", "arn:teste", 0);

		var facesContext = mock(FacesContext.class);

		try (var mocked = mockStatic(FacesContext.class)) {

			mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			controller.selecionarTopico(topico);

			controller.setBodyEnvio("{}");
			controller.setMessageAttributesEnvio("   ");

			controller.publicarMensagem();

			verify(snsService).publicarMensagem("arn:teste", null, "{}", "   ");
		}
	}

	@Test
	@DisplayName("Deve selecionar tópico utilizando o nome informado na requisição")
	void deve_selecionar_topico_utilizando_nome() {

		var topico = criar_topico();

		when(snsService.listarTopicos()).thenReturn(List.of(topico));

		when(snsService.listarAssinaturas(topico.getArn())).thenReturn(List.of(criar_assinatura()));

		when(navigationManager.getRequestedResource(ApplicationRoute.SNS)).thenReturn(Optional.of(topico.getNome()));

		var facesContext = mock(FacesContext.class);

		try (var mocked = mockStatic(FacesContext.class)) {

			mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			controller.init();

			assertThat(controller.getTopicoSelecionado()).isSameAs(topico);
		}
	}

	@Test
	@DisplayName("Não deve publicar mensagem quando corpo for nulo")
	void nao_deve_publicar_mensagem_quando_corpo_for_nulo() {

		when(snsService.listarAssinaturas(any())).thenReturn(List.of());

		var topico = criar_topico();

		var facesContext = mock(FacesContext.class);

		try (var mockedFacesContext = mockStatic(FacesContext.class)) {

			mockedFacesContext.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			controller.selecionarTopico(topico);

			controller.setBodyEnvio(null);

			controller.publicarMensagem();

			verify(snsService, never()).publicarMensagem(any(), any(), any(), any());

			verify(facesContext, atLeastOnce()).addMessage(isNull(), any(FacesMessage.class));
		}
	}
	
	@Test
	@DisplayName("Deve limpar assinaturas quando não houver tópico selecionado")
	void deve_limpar_assinaturas_quando_nao_houver_topico_selecionado() {

		ReflectionTestUtils.setField(controller, "topicoSelecionado", null);

		ReflectionTestUtils.setField(controller, "assinaturas",
				new ArrayList<>(List.of(criar_assinatura())));

		ReflectionTestUtils.invokeMethod(controller, "carregarAssinaturas");

		assertThat(controller.getAssinaturas()).isEmpty();

		verifyNoInteractions(snsService);
	}
}