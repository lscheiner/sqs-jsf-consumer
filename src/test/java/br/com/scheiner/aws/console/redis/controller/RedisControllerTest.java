package br.com.scheiner.aws.console.redis.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.primefaces.PrimeFaces;

import br.com.scheiner.aws.console.redis.model.RedisConfiguracao;
import br.com.scheiner.aws.console.redis.model.RedisRegistro;
import br.com.scheiner.aws.console.redis.service.RedisService;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

class RedisControllerTest {

	private final RedisService redisService = mock(RedisService.class);
	private final RedisController controller = new RedisController(redisService);

	@DisplayName("Deve formatar TTL corretamente")
	@ParameterizedTest(name = "TTL {0} deve formatar como \"{1}\"")
	@CsvSource(value = {
		"NIL, -",
		"-1, Sem expiração",
		"-2, Expirado",
		"30, 30 segundos",
		"999, 999 segundos"
	}, nullValues = "NIL")
	void deve_formatar_ttl_corretamente(Long ttl, String resultadoEsperado) {
		assertThat(controller.formatarTtl(ttl)).isEqualTo(resultadoEsperado);
	}

	@Test
	@DisplayName("Deve gerar preview vazio para valor nulo ou em branco")
	void deve_gerar_preview_vazio_para_valor_nulo_ou_em_branco() {
		assertThat(controller.previewValor(null)).isEmpty();
		assertThat(controller.previewValor("   ")).isEmpty();
	}

	@Test
	@DisplayName("Deve normalizar espacos e limitar preview de valores longos")
	void deve_normalizar_espacos_e_limitar_preview_de_valores_longos() {
		
		var valorLongo = "a".repeat(130);
		assertThat(controller.previewValor("valor\n com\t espacos")).isEqualTo("valor com espacos");
		assertThat(controller.previewValor(valorLongo)).hasSize(123).endsWith("...");
	}

	@Test
	@DisplayName("Deve atualizar registros usando o servico")
	void deve_atualizar_registros_usando_o_servico() {
		
		var registro = new RedisRegistro();
		when(redisService.listarRegistros()).thenReturn(List.of(registro));

		controller.atualizarRegistros();

		assertThat(controller.getRegistros()).containsExactly(registro);
	}

	@Test
	@DisplayName("Deve atualizar registros via grid com sucesso")
	void deve_atualizar_registros_grid_com_sucesso() {
		
		when(redisService.listarRegistros()).thenReturn(List.of());

		try (var facesContextMockedStatic = mockStatic(FacesContext.class)) {
			var facesContext = mock(FacesContext.class);
			facesContextMockedStatic.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			controller.atualizarRegistrosGrid();

			verify(facesContext).addMessage(any(), any(FacesMessage.class));
			assertThat(controller.getRegistros()).isEmpty();
		}
	}

	@Test
	@DisplayName("Deve tratar erro ao atualizar registros via grid")
	void deve_tratar_erro_ao_atualizar_registros_grid() {
		
		when(redisService.listarRegistros()).thenThrow(new RuntimeException("Erro de IO"));

		try (var facesContextMockedStatic = mockStatic(FacesContext.class)) {
			var facesContext = mock(FacesContext.class);
			facesContextMockedStatic.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			controller.atualizarRegistrosGrid();

			verify(facesContext).addMessage(any(), any(FacesMessage.class));
			assertThat(controller.getRegistros()).isEmpty();
		}
	}

	@Test
	@DisplayName("Deve preparar formulario para novo registro")
	void deve_preparar_formulario_para_novo_registro() {
		
		controller.setChaveFormulario("a");
		controller.setValorFormulario("b");
		controller.setTtlFormulario(10L);

		controller.novoRegistro();

		assertThat(controller.getChaveFormulario()).isNull();
		assertThat(controller.getValorFormulario()).isNull();
		assertThat(controller.getTtlFormulario()).isNull();
		assertThat(controller.isNovoRegistro()).isTrue();
	}

	@Test
	@DisplayName("Deve preencher formulario ao editar registro")
	void deve_preencher_formulario_ao_editar_registro() {
		
		var registro = new RedisRegistro();
		registro.setChave("k");
		registro.setValor("v");
		registro.setTtl(-1L);

		controller.editarRegistro(registro);

		assertThat(controller.getChaveFormulario()).isEqualTo("k");
		assertThat(controller.getValorFormulario()).isEqualTo("v");
		assertThat(controller.getTtlFormulario()).isNull();
		assertThat(controller.isNovoRegistro()).isFalse();
	}

	@Test
	@DisplayName("Deve selecionar registro para visualizacao somente leitura")
	void deve_selecionar_registro_para_visualizacao() {
		var registro = new RedisRegistro("usuario:1", "{\"nome\":\"Leandro\"}", "hash", -1L);

		controller.visualizarRegistro(registro);

		assertThat(controller.getRegistroVisualizacao()).isSameAs(registro);
	}

	@Test
	@DisplayName("Deve manter TTL positivo ao editar registro")
	void deve_manter_ttl_positivo_ao_editar_registro() {
		var registro = new RedisRegistro();
		registro.setChave("k");
		registro.setValor("v");
		registro.setTtl(15L);

		controller.editarRegistro(registro);

		assertThat(controller.getTtlFormulario()).isEqualTo(15L);
	}

	@Test
	@DisplayName("Deve salvar registro editado alterando a chave antiga")
	void deve_salvar_registro_editado_alterando_a_chave() {
		var registro = new RedisRegistro();
		registro.setChave("chave-antiga");
		controller.editarRegistro(registro);

		controller.setChaveFormulario("chave-nova");

		try (var primeFacesMockedStatic = mockStatic(PrimeFaces.class);
				var facesContextMockedStatic = mockStatic(FacesContext.class)) {

			var primeFaces = mock(PrimeFaces.class);
			var ajax = mock(PrimeFaces.Ajax.class);
			var facesContext = mock(FacesContext.class);

			when(primeFaces.ajax()).thenReturn(ajax);
			primeFacesMockedStatic.when(PrimeFaces::current).thenReturn(primeFaces);
			facesContextMockedStatic.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			controller.salvarRegistro();

			verify(redisService).salvarRegistro("chave-nova", null, null);
			verify(redisService).excluirRegistro("chave-antiga");
			verify(ajax).addCallbackParam("salvo", true);
		}
	}

	@Test
	@DisplayName("Deve tratar erro ao falhar em salvar registro")
	void deve_tratar_erro_ao_falhar_em_salvar_registro() {
		doThrow(new RuntimeException("Erro ao persistir")).when(redisService).salvarRegistro(any(), any(), any());

		try (var primeFacesMockedStatic = mockStatic(PrimeFaces.class);
				var facesContextMockedStatic = mockStatic(FacesContext.class)) {

			var primeFaces = mock(PrimeFaces.class);
			var ajax = mock(PrimeFaces.Ajax.class);
			var facesContext = mock(FacesContext.class);

			when(primeFaces.ajax()).thenReturn(ajax);
			primeFacesMockedStatic.when(PrimeFaces::current).thenReturn(primeFaces);
			facesContextMockedStatic.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			controller.salvarRegistro();

			verify(ajax).addCallbackParam("salvo", false);
			verify(facesContext).addMessage(any(), any(FacesMessage.class));
		}
	}


	@Test
	@DisplayName("Deve tratar erro ao falhar em excluir registro")
	void deve_tratar_erro_ao_falhar_em_excluir_registro() {
		var registro = new RedisRegistro();
		registro.setChave("remover");
		doThrow(new RuntimeException("Lock no Redis")).when(redisService).excluirRegistro("remover");

		try (var facesContextMockedStatic = mockStatic(FacesContext.class)) {
			var facesContext = mock(FacesContext.class);
			facesContextMockedStatic.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			controller.excluirRegistro(registro);

			verify(facesContext).addMessage(any(), any(FacesMessage.class));
		}
	}

	@Test
	@DisplayName("Deve reverter configuracao ao capturar erro em aplicarConfiguracao")
	void deve_reverter_configuracao_ao_capturar_erro() {
		doThrow(new RuntimeException("Host inacessível")).when(redisService).aplicarConfiguracao(any());
		when(redisService.carregarConfiguracao()).thenReturn(new RedisConfiguracao("backup", 32, false, "b", "p"));

		try (var facesContextMockedStatic = mockStatic(FacesContext.class)) {
			var facesContext = mock(FacesContext.class);
			facesContextMockedStatic.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			controller.aplicarConfiguracao();

			assertThat(controller.getHost()).isEqualTo("backup");
			assertThat(controller.getPort()).isEqualTo(32);
			assertThat(controller.getTls()).isFalse();
			assertThat(controller.getUsername()).isEqualTo("b");
			assertThat(controller.getPassword()).isEqualTo("p");
		}
	}

	@Test
	@DisplayName("Deve salvar registro editado sem excluir a chave quando ela não foi alterada")
	void deve_salvar_registro_editado_sem_excluir_quando_chave_nao_foi_alterada() {
		var registro = new RedisRegistro();
		registro.setChave("mesma-chave");
		registro.setValor("valor");

		controller.editarRegistro(registro);

		controller.setChaveFormulario("mesma-chave");
		controller.setValorFormulario("novo-valor");

		try (var primeFacesMockedStatic = mockStatic(PrimeFaces.class);
				var facesContextMockedStatic = mockStatic(FacesContext.class)) {

			var primeFaces = mock(PrimeFaces.class);
			var ajax = mock(PrimeFaces.Ajax.class);
			var facesContext = mock(FacesContext.class);

			when(primeFaces.ajax()).thenReturn(ajax);

			primeFacesMockedStatic.when(PrimeFaces::current).thenReturn(primeFaces);
			facesContextMockedStatic.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			when(redisService.listarRegistros()).thenReturn(List.of());

			controller.salvarRegistro();

			verify(redisService).salvarRegistro("mesma-chave", "novo-valor", null);
			verify(redisService, never()).excluirRegistro(any());
			verify(ajax).addCallbackParam("salvo", true);

			assertThat(controller.isNovoRegistro()).isFalse();
		}
	}

	@Test
	@DisplayName("Deve preencher formulário ao editar registro com TTL nulo")
	void deve_preencher_formulario_ao_editar_registro_com_ttl_nulo() {
		var registro = new RedisRegistro();
		registro.setChave("chave");
		registro.setValor("valor");
		registro.setTtl(null);

		controller.editarRegistro(registro);

		assertThat(controller.getChaveFormulario()).isEqualTo("chave");
		assertThat(controller.getValorFormulario()).isEqualTo("valor");
		assertThat(controller.getTtlFormulario()).isNull();
		assertThat(controller.isNovoRegistro()).isFalse();
	}

	@Test
	@DisplayName("Deve preencher formulário ao editar registro com TTL igual a zero")
	void deve_preencher_formulario_ao_editar_registro_com_ttl_zero() {
		var registro = new RedisRegistro();
		registro.setChave("chave");
		registro.setValor("valor");
		registro.setTtl(0L);

		controller.editarRegistro(registro);

		assertThat(controller.getTtlFormulario()).isNull();
	}

	@Test
	@DisplayName("Deve retornar preview completo quando possuir exatamente cento e vinte caracteres")
	void deve_retornar_preview_completo_quando_possuir_exatamente_cento_e_vinte_caracteres() {
		var valor = "a".repeat(120);

		assertThat(controller.previewValor(valor)).isEqualTo(valor);
	}
	
	@Test
	@DisplayName("Deve aplicar configuração com sucesso")
	void deve_aplicar_configuracao_com_sucesso() {
		controller.setHost("localhost");
		controller.setPort(6379);
		controller.setTls(true);
		controller.setUsername("user");
		controller.setPassword("pass");

		when(redisService.testarConexao()).thenReturn(true);
		when(redisService.listarRegistros()).thenReturn(List.of());

		try (var facesContextMockedStatic = mockStatic(FacesContext.class)) {
			var facesContext = mock(FacesContext.class);
			facesContextMockedStatic.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			controller.aplicarConfiguracao();

			verify(redisService).aplicarConfiguracao(any(RedisConfiguracao.class));
			verify(redisService).testarConexao();
			verify(redisService).listarRegistros();

			assertThat(controller.isConnected()).isTrue();
			assertThat(controller.getRegistros()).isEmpty();
		}
	}
	
	@Test
	@DisplayName("Deve excluir registro com sucesso")
	void deve_excluir_registro_com_sucesso() {
		var registro = new RedisRegistro();
		registro.setChave("remover");

		when(redisService.listarRegistros()).thenReturn(List.of());

		try (var facesContextMockedStatic = mockStatic(FacesContext.class)) {
			var facesContext = mock(FacesContext.class);
			facesContextMockedStatic.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			controller.excluirRegistro(registro);

			verify(redisService).excluirRegistro("remover");
			verify(redisService).listarRegistros();

			assertThat(controller.getRegistros()).isEmpty();

			verify(facesContext).addMessage(any(), any(FacesMessage.class));
		}
	}
	
	@Test
	@DisplayName("Deve salvar novo registro com sucesso")
	void deve_salvar_novo_registro_com_sucesso() {
		controller.novoRegistro();
		controller.setChaveFormulario("nova-chave");
		controller.setValorFormulario("valor");

		when(redisService.listarRegistros()).thenReturn(List.of());

		try (var primeFacesMockedStatic = mockStatic(PrimeFaces.class);
			 var facesContextMockedStatic = mockStatic(FacesContext.class)) {

			var primeFaces = mock(PrimeFaces.class);
			var ajax = mock(PrimeFaces.Ajax.class);
			var facesContext = mock(FacesContext.class);

			when(primeFaces.ajax()).thenReturn(ajax);

			primeFacesMockedStatic.when(PrimeFaces::current).thenReturn(primeFaces);
			facesContextMockedStatic.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			controller.salvarRegistro();

			verify(redisService).salvarRegistro("nova-chave", "valor", null);
			verify(redisService, never()).excluirRegistro(any());
			verify(redisService).listarRegistros();

			assertThat(controller.getRegistros()).isEmpty();

			verify(ajax).addCallbackParam("salvo", true);
		}
	}
	
	@Test
	@DisplayName("Deve testar conexão com sucesso")
	void deve_testar_conexao_com_sucesso() {
		when(redisService.testarConexao()).thenReturn(true);

		try (var facesContextMockedStatic = mockStatic(FacesContext.class)) {
			var facesContext = mock(FacesContext.class);

			facesContextMockedStatic.when(FacesContext::getCurrentInstance)
					.thenReturn(facesContext);

			controller.testarConexao();

			verify(redisService).testarConexao();

			assertThat(controller.isConnected()).isTrue();

			verify(facesContext).addMessage(any(), any(FacesMessage.class));
		}
	}
	
	@Test
	@DisplayName("Deve testar conexão com falha")
	void deve_testar_conexao_com_falha() {
		when(redisService.testarConexao()).thenReturn(false);

		try (var facesContextMockedStatic = mockStatic(FacesContext.class)) {
			var facesContext = mock(FacesContext.class);

			facesContextMockedStatic.when(FacesContext::getCurrentInstance)
					.thenReturn(facesContext);

			controller.testarConexao();

			verify(redisService).testarConexao();

			assertThat(controller.isConnected()).isFalse();

			verify(facesContext).addMessage(any(), any(FacesMessage.class));
		}
	}
	
	@Test
	@DisplayName("Deve inicializar configuração e registros no PostConstruct")
	void deve_inicializar_configuracao_e_registros() {
		when(redisService.carregarConfiguracao())
				.thenReturn(new RedisConfiguracao("h", 1, true, "u", "p"));
		when(redisService.testarConexao()).thenReturn(true);
		when(redisService.listarRegistros()).thenReturn(List.of());

		try (var primeFacesMockedStatic = mockStatic(PrimeFaces.class);
			 var facesContextMockedStatic = mockStatic(FacesContext.class)) {

			var primeFaces = mock(PrimeFaces.class);
			var ajax = mock(PrimeFaces.Ajax.class);
			var facesContext = mock(FacesContext.class);

			when(primeFaces.ajax()).thenReturn(ajax);

			primeFacesMockedStatic.when(PrimeFaces::current).thenReturn(primeFaces);
			facesContextMockedStatic.when(FacesContext::getCurrentInstance)
					.thenReturn(facesContext);

			controller.init();

			assertThat(controller.getHost()).isEqualTo("h");
			assertThat(controller.getPort()).isEqualTo(1);
			assertThat(controller.getTls()).isTrue();
			assertThat(controller.getUsername()).isEqualTo("u");
			assertThat(controller.getPassword()).isEqualTo("p");
			assertThat(controller.isConnected()).isTrue();
			assertThat(controller.getRegistros()).isEmpty();

			verify(redisService).listarRegistros();
		}
	}
}
