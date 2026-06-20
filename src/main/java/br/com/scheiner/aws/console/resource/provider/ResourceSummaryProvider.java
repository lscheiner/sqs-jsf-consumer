package br.com.scheiner.aws.console.resource.provider;

import br.com.scheiner.aws.console.resource.model.ResourceSnapshot;
import br.com.scheiner.aws.console.resource.model.ResourceType;

public interface ResourceSummaryProvider {

	ResourceType getType();

	ResourceSnapshot load();
}
