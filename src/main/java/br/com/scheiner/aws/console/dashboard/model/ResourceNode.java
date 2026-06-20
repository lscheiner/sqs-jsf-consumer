package br.com.scheiner.aws.console.dashboard.model;

import java.io.Serializable;

import br.com.scheiner.aws.console.resource.model.ResourceType;

public class ResourceNode implements Serializable {

	private static final long serialVersionUID = 1L;

	private ResourceType type;
	private String name;
	private String identifier;
	private boolean resource;

	public ResourceNode() {
	}

	public ResourceNode(ResourceType type, String name, String identifier, boolean resource) {
		this.type = type;
		this.name = name;
		this.identifier = identifier;
		this.resource = resource;
	}

	public static ResourceNode group(ResourceType type) {
		return new ResourceNode(type, type.getDisplayName(), null, false);
	}

	public static ResourceNode resource(ResourceType type, String name, String identifier) {
		return new ResourceNode(type, name, identifier, true);
	}

	public ResourceType getType() {
		return this.type;
	}

	public void setType(ResourceType type) {
		this.type = type;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIdentifier() {
		return this.identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public boolean isResource() {
		return this.resource;
	}

	public void setResource(boolean resource) {
		this.resource = resource;
	}

	@Override
	public String toString() {
		return this.name;
	}
}
