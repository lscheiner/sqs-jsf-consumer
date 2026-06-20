package br.com.scheiner.aws.console.resource.model;

public enum ResourceType {

	SQS("SQS"),
	DYNAMODB("DynamoDB"),
	REDIS("Redis"),
	SNS("SNS"),
	ROOT("Resources");

	private final String displayName;

	ResourceType(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return this.displayName;
	}
}
