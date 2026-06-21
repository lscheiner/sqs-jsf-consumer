package br.com.scheiner.aws.console.resource.provider;

import br.com.scheiner.aws.console.resource.model.ResourceInfo;
import br.com.scheiner.aws.console.resource.model.ResourceType;

public interface ResourceInfoProvider {

	ResourceType getType();

	ResourceInfo load();
}
