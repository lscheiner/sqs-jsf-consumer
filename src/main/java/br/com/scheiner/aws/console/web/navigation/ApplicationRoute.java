package br.com.scheiner.aws.console.web.navigation;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import br.com.scheiner.aws.console.resource.model.ResourceType;

public enum ApplicationRoute {

	DASHBOARD("/dashboard.xhtml", null),
	SQS("/sqs-explorer.xhtml", "fila"),
	SNS("/sns.xhtml", "topico"),
	DYNAMODB("/dynamodb.xhtml", "tabela"),
	REDIS("/redis.xhtml", null);

	private final String path;
	private final String resourceParameter;

	ApplicationRoute(String path, String resourceParameter) {
		this.path = path;
		this.resourceParameter = resourceParameter;
	}

	public String getPath() {
		return this.path;
	}

	public String getResourceParameter() {
		return this.resourceParameter;
	}

	public String getPath(String resourceIdentifier) {
		if (this.resourceParameter == null || resourceIdentifier == null || resourceIdentifier.isBlank()) {
			return this.path;
		}
		return "%s?%s=%s".formatted(
				this.path,
				this.resourceParameter,
				URLEncoder.encode(resourceIdentifier, StandardCharsets.UTF_8));
	}

	public static ApplicationRoute from(ResourceType resourceType) {
		return switch (resourceType) {
			case SQS -> SQS;
			case SNS -> SNS;
			case DYNAMODB -> DYNAMODB;
			case REDIS -> REDIS;
			case ROOT -> null;
		};
	}
}
