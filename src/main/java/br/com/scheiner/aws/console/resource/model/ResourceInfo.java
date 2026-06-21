package br.com.scheiner.aws.console.resource.model;

import java.util.ArrayList;
import java.util.List;

public class ResourceInfo {

	private ResourceType type;
	private ServiceStatus status;
	private long count;
	private String configuredAddress;
	private List<ResourceDescriptor> resources = new ArrayList<>();

	public ResourceType getType() {
		return this.type;
	}

	public void setType(ResourceType type) {
		this.type = type;
	}

	public ServiceStatus getStatus() {
		return this.status;
	}

	public void setStatus(ServiceStatus status) {
		this.status = status;
	}

	public long getCount() {
		return this.count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public String getConfiguredAddress() {
		return this.configuredAddress;
	}

	public void setConfiguredAddress(String configuredAddress) {
		this.configuredAddress = configuredAddress;
	}

	public List<ResourceDescriptor> getResources() {
		return this.resources;
	}

	public void setResources(List<ResourceDescriptor> resources) {
		this.resources = resources;
	}
}
