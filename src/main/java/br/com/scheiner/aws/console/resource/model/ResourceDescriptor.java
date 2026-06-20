package br.com.scheiner.aws.console.resource.model;

public class ResourceDescriptor {

	private ResourceType type;
	private String name;
	private String identifier;

	public ResourceDescriptor() {
	}

	public ResourceDescriptor(ResourceType type, String name, String identifier) {
		this.type = type;
		this.name = name;
		this.identifier = identifier;
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
}
