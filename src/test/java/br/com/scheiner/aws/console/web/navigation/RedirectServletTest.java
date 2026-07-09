package br.com.scheiner.aws.console.web.navigation;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class RedirectServletTest {

	@Test
	@DisplayName("Deve redirecionar raiz da aplicacao para o dashboard")
	void deve_redirecionar_raiz_da_aplicacao_para_o_dashboard() throws Exception {
		var request = mock(HttpServletRequest.class);
		var response = mock(HttpServletResponse.class);
		when(request.getContextPath()).thenReturn("/aws-console");

		new ServletTestavel().executarDoGet(request, response);

		verify(response).sendRedirect("/aws-console/dashboard.xhtml");
	}

	private static class ServletTestavel extends RedirectServlet {

		private static final long serialVersionUID = 1L;

		void executarDoGet(HttpServletRequest request, HttpServletResponse response) {
			super.doGet(request, response);
		}
	}
}
