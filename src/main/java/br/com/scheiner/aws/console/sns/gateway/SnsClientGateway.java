package br.com.scheiner.aws.console.sns.gateway;

import software.amazon.awssdk.services.sns.SnsClient;

public interface SnsClientGateway {

	SnsClient getClient();
}
