package br.com.scheiner.aws.console.dashboard.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.EnumMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import br.com.scheiner.aws.console.dashboard.model.DashboardData;
import br.com.scheiner.aws.console.dashboard.model.DashboardSummary;
import br.com.scheiner.aws.console.dashboard.model.ResourceNode;
import br.com.scheiner.aws.console.dashboard.service.DashboardService;
import br.com.scheiner.aws.console.resource.model.ResourceType;
import br.com.scheiner.aws.console.resource.model.ServiceStatus;
import br.com.scheiner.aws.console.web.navigation.ApplicationRoute;
import br.com.scheiner.aws.console.web.navigation.NavigationManager;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

class DashboardControllerTest {

	private DashboardService dashboardService;
	private NavigationManager navigationManager;
	private DashboardController controller;

	@BeforeEach
	void setup() {
		dashboardService = mock(DashboardService.class);
		navigationManager = mock(NavigationManager.class);

		controller = new DashboardController(dashboardService, navigationManager);
	}

	private DashboardData criarDashboard() {

		var summary = new DashboardSummary();

		var resources = new EnumMap<ResourceType, List<ResourceNode>>(ResourceType.class);

		resources.put(ResourceType.SQS, List.of(ResourceNode.resource(ResourceType.SQS, "fila-1", "fila-1")));

		resources.put(ResourceType.SNS, List.of(ResourceNode.resource(ResourceType.SNS, "topico-1", "topico-1")));

		resources.put(ResourceType.DYNAMODB,
				List.of(ResourceNode.resource(ResourceType.DYNAMODB, "tabela-1", "tabela-1")));

		resources.put(ResourceType.REDIS, List.of(ResourceNode.resource(ResourceType.REDIS, "redis", "redis")));

		var data = new DashboardData();
		data.setSummary(summary);
		data.setResources(resources);

		return data;
	}

	@Test
	@DisplayName("Deve tratar erro ao atualizar dashboard")
	void deve_tratar_erro_ao_atualizar_dashboard() {

		when(dashboardService.load()).thenThrow(new RuntimeException("erro"));

		var facesContext = mock(FacesContext.class);

		try (var mockedFacesContext = mockStatic(FacesContext.class)) {

			mockedFacesContext.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			controller.refresh();

			verify(facesContext).addMessage(org.mockito.ArgumentMatchers.isNull(), any(FacesMessage.class));

			assertThat(controller.getSummary()).isNull();
			assertThat(controller.getResources()).isNull();
		}
	}

	@Test
	@DisplayName("Deve retornar summary")
	void deve_retornar_summary() {

		var data = criarDashboard();

		when(dashboardService.load()).thenReturn(data);

		controller.refresh();

		assertThat(controller.getSummary()).isSameAs(data.getSummary());
	}

	@Test
	@DisplayName("Deve retornar recursos")
	void deve_retornar_recursos() {

		var data = criarDashboard();

		when(dashboardService.load()).thenReturn(data);

		controller.refresh();

		assertThat(controller.getResources()).isNotNull();

		assertThat(controller.getResources().getChildren()).hasSize(4);
	}

	@Test
	@DisplayName("Deve mapear severidade visual a partir do status")
	void deve_mapear_severidade_visual_a_partir_do_status() {

		assertThat(controller.statusSeverity(ServiceStatus.CONNECTED)).isEqualTo("success");

		assertThat(controller.statusSeverity(ServiceStatus.DISCONNECTED)).isEqualTo("danger");

		assertThat(controller.statusSeverity(ServiceStatus.UNAVAILABLE)).isEqualTo("danger");
	}

	@Test
	@DisplayName("Deve mapear label visual a partir do status")
	void deve_mapear_label_visual_a_partir_do_status() {

		assertThat(controller.statusLabel(ServiceStatus.CONNECTED)).isEqualTo("Connected");

		assertThat(controller.statusLabel(ServiceStatus.DISCONNECTED)).isEqualTo("Disconnected");

		assertThat(controller.statusLabel(ServiceStatus.UNAVAILABLE)).isEqualTo("Unavailable");
	}

	@Test
	@DisplayName("Não deve navegar quando o nó selecionado for um grupo")
	void nao_deve_navegar_quando_no_selecionado_for_grupo() throws IOException {

		TreeNode<ResourceNode> node = new DefaultTreeNode<>(ResourceNode.group(ResourceType.REDIS));

		NodeSelectEvent event = mock(NodeSelectEvent.class);

		when(event.getTreeNode()).thenReturn(node);

		controller.onNodeSelect(event);

		verifyNoInteractions(navigationManager);
	}

	@Test
	@DisplayName("Deve navegar quando o nó selecionado representar um recurso")
	void deve_navegar_quando_no_selecionado_representar_um_recurso() throws IOException {

		TreeNode<ResourceNode> node = new DefaultTreeNode<>(
				ResourceNode.resource(ResourceType.REDIS, "Redis Local", "redis-local"));

		NodeSelectEvent event = mock(NodeSelectEvent.class);

		when(event.getTreeNode()).thenReturn(node);

		controller.onNodeSelect(event);

		verify(navigationManager).redirect(ApplicationRoute.REDIS, "redis-local");
	}

	@Test
	@DisplayName("Não deve navegar quando o nó possuir recurso nulo")
	void nao_deve_navegar_quando_no_possuir_recurso_nulo() throws IOException {

		TreeNode<ResourceNode> node = new DefaultTreeNode<>(null);

		NodeSelectEvent event = mock(NodeSelectEvent.class);

		when(event.getTreeNode()).thenReturn(node);

		controller.onNodeSelect(event);

		verifyNoInteractions(navigationManager);
	}

	@Test
	@DisplayName("Deve abrir rota informada")
	void deve_abrir_rota_informada() throws IOException {

		controller.abrir("REDIS");

		verify(navigationManager).redirect(ApplicationRoute.REDIS);
	}

	@Test
	@DisplayName("Deve inicializar carregando dashboard")
	void deve_inicializar_carregando_dashboard() {

		var data = criarDashboard();

		when(dashboardService.load()).thenReturn(data);

		controller.init();

		assertThat(controller.getSummary()).isSameAs(data.getSummary());
		assertThat(controller.getResources()).isNotNull();

		assertThat(controller.getResources().getChildren()).extracting(TreeNode::getData)
				.extracting(ResourceNode::getType)
				.containsExactly(ResourceType.SQS, ResourceType.DYNAMODB, ResourceType.REDIS, ResourceType.SNS);
	}

	@Test
	@DisplayName("Deve atualizar dashboard")
	void deve_atualizar_dashboard() {

		var data = criarDashboard();

		when(dashboardService.load()).thenReturn(data);

		controller.refresh();

		assertThat(controller.getSummary()).isSameAs(data.getSummary());
		assertThat(controller.getResources()).isNotNull();

		assertThat(controller.getResources().getChildren()).extracting(TreeNode::getData)
				.extracting(ResourceNode::getType)
				.containsExactly(ResourceType.SQS, ResourceType.DYNAMODB, ResourceType.REDIS, ResourceType.SNS);
	}
}