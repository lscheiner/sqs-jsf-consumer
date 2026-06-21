package br.com.scheiner.aws.console.dashboard.controller;

import java.io.IOException;
import java.util.List;

import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.scheiner.aws.console.controller.Controller;
import br.com.scheiner.aws.console.dashboard.model.DashboardSummary;
import br.com.scheiner.aws.console.dashboard.model.ResourceNode;
import br.com.scheiner.aws.console.dashboard.service.DashboardService;
import br.com.scheiner.aws.console.resource.model.ResourceType;
import br.com.scheiner.aws.console.resource.model.ServiceStatus;
import br.com.scheiner.aws.console.web.navigation.ApplicationRoute;
import br.com.scheiner.aws.console.web.navigation.NavigationManager;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

@Named
@ViewScoped
public class DashboardController implements Controller {

	private static final Logger LOGGER = LoggerFactory.getLogger(DashboardController.class);

	private final DashboardService dashboardService;
	private final NavigationManager navigationManager;
	private DashboardSummary summary;
	private TreeNode<ResourceNode> resources;

	public DashboardController(DashboardService dashboardService, NavigationManager navigationManager) {
		this.dashboardService = dashboardService;
		this.navigationManager = navigationManager;
	}

	@PostConstruct
	public void init() {
		this.refresh();
	}

	public void refresh() {
		try {
			var data = this.dashboardService.load();
			this.summary = data.getSummary();
			this.resources = new DefaultTreeNode<>(ResourceNode.group(ResourceType.ROOT), null);
			this.addGroup(ResourceType.SQS, data.getResources(ResourceType.SQS));
			this.addGroup(ResourceType.DYNAMODB, data.getResources(ResourceType.DYNAMODB));
			this.addGroup(ResourceType.REDIS, data.getResources(ResourceType.REDIS));
			this.addGroup(ResourceType.SNS, data.getResources(ResourceType.SNS));
		} catch (Exception exception) {
			LOGGER.error("Erro carregando Dashboard", exception);
			this.adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Erro", "Nao foi possivel atualizar o Dashboard.");
		}
	}

	private void addGroup(ResourceType type, List<ResourceNode> children) {
		var group = new DefaultTreeNode<>(ResourceNode.group(type), this.resources);
		group.setExpanded(true);
		children.forEach(resource -> new DefaultTreeNode<>(resource, group));
	}

	public void onNodeSelect(NodeSelectEvent event) throws IOException {
		var resource = (ResourceNode) event.getTreeNode().getData();
		if (resource == null || !resource.isResource()) {
			return;
		}
		this.navigationManager.redirect(ApplicationRoute.from(resource.getType()), resource.getIdentifier());
	}

	public void abrir(String path) throws IOException {
		this.navigationManager.redirect(ApplicationRoute.valueOf(path));
	}

	public DashboardSummary getSummary() {
		return this.summary;
	}

	public TreeNode<ResourceNode> getResources() {
		return this.resources;
	}

	public String statusSeverity(ServiceStatus status) {
		return status == ServiceStatus.CONNECTED ? "success" : "danger";
	}

	public String statusLabel(ServiceStatus status) {
		return switch (status) {
			case CONNECTED -> "Connected";
			case DISCONNECTED -> "Disconnected";
			case UNAVAILABLE -> "Unavailable";
		};
	}
}
