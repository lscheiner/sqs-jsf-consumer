package br.com.scheiner.aws.console.web.navigation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;

class NavigationManagerTest {

	private final NavigationManager navigationManager = new NavigationManager();

	@Test
	@DisplayName("Deve retornar vazio quando FacesContext for nulo")
	void deve_retornar_vazio_quando_faces_context_for_nulo() {

		try (MockedStatic<FacesContext> mocked = mockStatic(FacesContext.class)) {

			mocked.when(FacesContext::getCurrentInstance).thenReturn(null);

			var route = mock(ApplicationRoute.class);

			assertThat(navigationManager.getRequestedResource(route)).isEmpty();
		}
	}

	@Test
	@DisplayName("Deve retornar vazio quando a rota não possuir parâmetro de recurso")
	void deve_retornar_vazio_quando_rota_nao_possuir_parametro_recurso() {

		var facesContext = mock(FacesContext.class);

		try (MockedStatic<FacesContext> mocked = mockStatic(FacesContext.class)) {

			mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			var route = mock(ApplicationRoute.class);
			when(route.getResourceParameter()).thenReturn(null);

			assertThat(navigationManager.getRequestedResource(route)).isEmpty();
		}
	}

	@Test
	@DisplayName("Deve retornar vazio quando parâmetro não existir")
	void deve_retornar_vazio_quando_parametro_nao_existir() {

		var facesContext = mock(FacesContext.class);
		var externalContext = mock(ExternalContext.class);

		when(facesContext.getExternalContext()).thenReturn(externalContext);
		when(externalContext.getRequestParameterMap()).thenReturn(Map.of());

		try (MockedStatic<FacesContext> mocked = mockStatic(FacesContext.class)) {

			mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			var route = mock(ApplicationRoute.class);
			when(route.getResourceParameter()).thenReturn("id");

			assertThat(navigationManager.getRequestedResource(route)).isEmpty();
		}
	}

	@Test
	@DisplayName("Deve retornar vazio quando parâmetro estiver vazio")
	void deve_retornar_vazio_quando_parametro_estiver_vazio() {

		var facesContext = mock(FacesContext.class);
		var externalContext = mock(ExternalContext.class);

		when(facesContext.getExternalContext()).thenReturn(externalContext);
		when(externalContext.getRequestParameterMap()).thenReturn(Map.of("id", ""));

		try (MockedStatic<FacesContext> mocked = mockStatic(FacesContext.class)) {

			mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			var route = mock(ApplicationRoute.class);
			when(route.getResourceParameter()).thenReturn("id");

			assertThat(navigationManager.getRequestedResource(route)).isEmpty();
		}
	}

	@Test
	@DisplayName("Deve retornar vazio quando parâmetro possuir apenas espaços")
	void deve_retornar_vazio_quando_parametro_possuir_apenas_espacos() {

		var facesContext = mock(FacesContext.class);
		var externalContext = mock(ExternalContext.class);

		when(facesContext.getExternalContext()).thenReturn(externalContext);
		when(externalContext.getRequestParameterMap()).thenReturn(Map.of("id", "   "));

		try (MockedStatic<FacesContext> mocked = mockStatic(FacesContext.class)) {

			mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			var route = mock(ApplicationRoute.class);
			when(route.getResourceParameter()).thenReturn("id");

			assertThat(navigationManager.getRequestedResource(route)).isEmpty();
		}
	}

	@Test
	@DisplayName("Deve retornar recurso solicitado quando parâmetro existir")
	void deve_retornar_recurso_solicitado_quando_parametro_existir() {

		var facesContext = mock(FacesContext.class);
		var externalContext = mock(ExternalContext.class);

		when(facesContext.getExternalContext()).thenReturn(externalContext);
		when(externalContext.getRequestParameterMap()).thenReturn(Map.of("id", "123"));

		try (MockedStatic<FacesContext> mocked = mockStatic(FacesContext.class)) {

			mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			var route = mock(ApplicationRoute.class);
			when(route.getResourceParameter()).thenReturn("id");

			assertThat(navigationManager.getRequestedResource(route))
					.contains("123");
		}
	}

	@Test
	@DisplayName("Deve redirecionar utilizando identificador nulo")
	void deve_redirecionar_utilizando_identificador_nulo() throws IOException {

		var facesContext = mock(FacesContext.class);
		var externalContext = mock(ExternalContext.class);

		when(facesContext.getExternalContext()).thenReturn(externalContext);
		when(externalContext.getRequestContextPath()).thenReturn("/app");

		var route = mock(ApplicationRoute.class);
		when(route.getPath(null)).thenReturn("/home");

		doNothing().when(externalContext).redirect(anyString());

		try (MockedStatic<FacesContext> mocked = mockStatic(FacesContext.class)) {

			mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			navigationManager.redirect(route);

			verify(externalContext).redirect("/app/home");
		}
	}

	@Test
	@DisplayName("Deve redirecionar utilizando identificador informado")
	void deve_redirecionar_utilizando_identificador_informado() throws IOException {

		var facesContext = mock(FacesContext.class);
		var externalContext = mock(ExternalContext.class);

		when(facesContext.getExternalContext()).thenReturn(externalContext);
		when(externalContext.getRequestContextPath()).thenReturn("/contexto");

		var route = mock(ApplicationRoute.class);
		when(route.getPath("123")).thenReturn("/usuarios/123");

		doNothing().when(externalContext).redirect(anyString());

		try (MockedStatic<FacesContext> mocked = mockStatic(FacesContext.class)) {

			mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			navigationManager.redirect(route, "123");

			verify(externalContext).redirect("/contexto/usuarios/123");
		}
	}

	@Test
	@DisplayName("Não deve redirecionar quando rota for nula")
	void nao_deve_redirecionar_quando_rota_for_nula() throws IOException {

		var facesContext = mock(FacesContext.class);

		try (MockedStatic<FacesContext> mocked = mockStatic(FacesContext.class)) {

			mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			navigationManager.redirect(null);

			verify(facesContext, never()).getExternalContext();
		}
	}

}