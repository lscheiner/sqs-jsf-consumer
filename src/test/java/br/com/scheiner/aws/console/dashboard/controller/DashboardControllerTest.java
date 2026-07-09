package br.com.scheiner.aws.console.dashboard.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.scheiner.aws.console.dashboard.service.DashboardService;
import br.com.scheiner.aws.console.resource.model.ServiceStatus;
import br.com.scheiner.aws.console.web.navigation.NavigationManager;

class DashboardControllerTest {

	private final DashboardController controller = new DashboardController(
			mock(DashboardService.class),
			mock(NavigationManager.class));

	@Test
	@DisplayName("Deve mapear severidade visual a partir do status")
	void deve_mapear_severidade_visual_a_partir_do_status() {
		assertThat(this.controller.statusSeverity(ServiceStatus.CONNECTED)).isEqualTo("success");
		assertThat(this.controller.statusSeverity(ServiceStatus.DISCONNECTED)).isEqualTo("danger");
		assertThat(this.controller.statusSeverity(ServiceStatus.UNAVAILABLE)).isEqualTo("danger");
	}

	@Test
	@DisplayName("Deve mapear label visual a partir do status")
	void deve_mapear_label_visual_a_partir_do_status() {
		assertThat(this.controller.statusLabel(ServiceStatus.CONNECTED)).isEqualTo("Connected");
		assertThat(this.controller.statusLabel(ServiceStatus.DISCONNECTED)).isEqualTo("Disconnected");
		assertThat(this.controller.statusLabel(ServiceStatus.UNAVAILABLE)).isEqualTo("Unavailable");
	}
}
