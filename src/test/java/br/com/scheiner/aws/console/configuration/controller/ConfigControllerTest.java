package br.com.scheiner.aws.console.configuration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.scheiner.aws.console.configuration.aws.AwsConfiguration;
import br.com.scheiner.aws.console.configuration.aws.AwsProvider;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import software.amazon.awssdk.regions.Region;

class ConfigControllerTest {

	private AwsProvider awsProvider;

	private AwsConfiguration awsConfiguration;

	private ConfigController controller;

	@BeforeEach
	void setup() {

		awsProvider = mock(AwsProvider.class);

		awsConfiguration = new AwsConfiguration("http://localhost:4566", "sa-east-1");

		controller = new ConfigController(List.of(awsProvider), awsConfiguration);
	}

	@Test
	@DisplayName("Deve inicializar configuração")
	void deve_inicializar_configuracao() {

		when(awsProvider.isConectado()).thenReturn(true);

		var facesContext = mock(FacesContext.class);

		try (var mocked = mockStatic(FacesContext.class)) {

			mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			controller.init();

			assertThat(controller.getEndpoint()).isEqualTo("http://localhost:4566");

			assertThat(controller.getRegion()).isEqualTo("sa-east-1");

			assertThat(controller.isConnected()).isTrue();

			verify(facesContext).addMessage(isNull(), any(FacesMessage.class));
		}
	}

	@Test
	@DisplayName("Deve retornar endpoint")
	void deve_retornar_endpoint() {

		controller.setEndpoint("http://novo-endpoint");

		assertThat(controller.getEndpoint()).isEqualTo("http://novo-endpoint");
	}

	@Test
	@DisplayName("Deve retornar região")
	void deve_retornar_regiao() {

		controller.setRegion("us-east-1");

		assertThat(controller.getRegion()).isEqualTo("us-east-1");
	}

	@Test
	@DisplayName("Deve retornar lista de provedores")
	void deve_retornar_lista_de_provedores() {

		assertThat(controller.getAwsProvider()).containsExactly(awsProvider);
	}

	@Test
	@DisplayName("Deve testar conexão com sucesso")
	void deve_testar_conexao_com_sucesso() {

		when(awsProvider.isConectado()).thenReturn(true);

		var facesContext = mock(FacesContext.class);

		try (var mocked = mockStatic(FacesContext.class)) {

			mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			controller.testarConexao();

			assertThat(controller.isConnected()).isTrue();

			verify(facesContext).addMessage(isNull(), any(FacesMessage.class));
		}
	}

	@Test
	@DisplayName("Deve testar conexão com erro")
	void deve_testar_conexao_com_erro() {

		when(awsProvider.isConectado()).thenReturn(false);

		var facesContext = mock(FacesContext.class);

		try (var mocked = mockStatic(FacesContext.class)) {

			mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			controller.testarConexao();

			assertThat(controller.isConnected()).isFalse();

			verify(facesContext).addMessage(isNull(), any(FacesMessage.class));
		}
	}

	@Test
	@DisplayName("Deve aplicar configuração com sucesso")
	void deve_aplicar_configuracao_com_sucesso() {

		when(awsProvider.isConectado()).thenReturn(true);

		var facesContext = mock(FacesContext.class);

		try (var mocked = mockStatic(FacesContext.class)) {

			mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			controller.setEndpoint("http://127.0.0.1:4566");
			controller.setRegion("us-east-1");

			controller.aplicarConfiguracao();

			assertThat(awsConfiguration.getEndpoint()).isEqualTo("http://127.0.0.1:4566");

			assertThat(awsConfiguration.getRegion()).isEqualTo(Region.US_EAST_1);

			assertThat(controller.isConnected()).isTrue();

			verify(awsProvider).reconfigurar();

			verify(facesContext).addMessage(isNull(), any(FacesMessage.class));
		}
	}

	@Test
	@DisplayName("Deve restaurar endpoint quando configuração for inválida")
	void deve_restaurar_endpoint_quando_configuracao_for_invalida() {

		var endpointOriginal = awsConfiguration.getEndpoint();

		var facesContext = mock(FacesContext.class);

		try (var mocked = mockStatic(FacesContext.class)) {

			mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			controller.setEndpoint("endpoint-invalido");
			controller.setRegion("us-east-1");

			controller.aplicarConfiguracao();

			assertThat(controller.getEndpoint()).isEqualTo(endpointOriginal);

			assertThat(awsConfiguration.getEndpoint()).isEqualTo(endpointOriginal);

			verify(facesContext).addMessage(isNull(), any(FacesMessage.class));
		}
	}

	@Test
	@DisplayName("Deve aplicar configuração utilizando HTTPS")
	void deve_aplicar_configuracao_utilizando_https() {

		when(awsProvider.isConectado()).thenReturn(true);

		var facesContext = mock(FacesContext.class);

		try (var mocked = mockStatic(FacesContext.class)) {

			mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			controller.setEndpoint("https://localhost:4566");
			controller.setRegion("sa-east-1");

			controller.aplicarConfiguracao();

			assertThat(awsConfiguration.getEndpoint()).isEqualTo("https://localhost:4566");

			assertThat(awsConfiguration.getRegion()).isEqualTo(Region.SA_EAST_1);

			verify(awsProvider).reconfigurar();
		}
	}

	@Test
	@DisplayName("Deve considerar conectado quando pelo menos um provider estiver conectado")
	void deve_considerar_conectado_quando_pelo_menos_um_provider_estiver_conectado() {

		var providerDesconectado = mock(AwsProvider.class);
		var providerConectado = mock(AwsProvider.class);

		when(providerDesconectado.isConectado()).thenReturn(false);
		when(providerConectado.isConectado()).thenReturn(true);

		controller = new ConfigController(List.of(providerDesconectado, providerConectado), awsConfiguration);

		var facesContext = mock(FacesContext.class);

		try (var mocked = mockStatic(FacesContext.class)) {

			mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			controller.testarConexao();

			assertThat(controller.isConnected()).isTrue();

			verify(facesContext).addMessage(isNull(), any(FacesMessage.class));
		}
	}

	@Test
	@DisplayName("Deve retornar lista de regiões ordenada")
	void deve_retornar_lista_de_regioes() {

		var regioes = controller.getRegions();

		assertThat(regioes).isNotEmpty().contains("sa-east-1", "us-east-1").isSorted();
	}

	@Test
	@DisplayName("Deve considerar desconectado quando nenhum provider estiver conectado")
	void deve_considerar_desconectado_quando_nenhum_provider_estiver_conectado() {

		var provider1 = mock(AwsProvider.class);
		var provider2 = mock(AwsProvider.class);

		when(provider1.isConectado()).thenReturn(false);
		when(provider2.isConectado()).thenReturn(false);

		controller = new ConfigController(List.of(provider1, provider2), awsConfiguration);

		var facesContext = mock(FacesContext.class);

		try (var mocked = mockStatic(FacesContext.class)) {

			mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			controller.testarConexao();

			assertThat(controller.isConnected()).isFalse();

			verify(facesContext).addMessage(isNull(), any(FacesMessage.class));
		}
	}
}