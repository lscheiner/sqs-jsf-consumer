package br.com.scheiner.aws.console.dashboard.model;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import br.com.scheiner.aws.console.resource.model.ResourceType;

public class DashboardData {

	private DashboardSummary summary;
	private Map<ResourceType, List<ResourceNode>> resources = new EnumMap<>(ResourceType.class);

	public DashboardSummary getSummary() {
		return this.summary;
	}

	public void setSummary(DashboardSummary summary) {
		this.summary = summary;
	}

	public Map<ResourceType, List<ResourceNode>> getResources() {
		return this.resources;
	}

	public void setResources(Map<ResourceType, List<ResourceNode>> resources) {
		this.resources = resources;
	}

	public List<ResourceNode> getResources(ResourceType type) {
		return this.resources.getOrDefault(type, List.of());
	}
}
