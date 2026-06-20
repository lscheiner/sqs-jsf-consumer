package br.com.scheiner.aws.console.sqs.gateway;

import software.amazon.awssdk.services.sqs.SqsClient;

public interface SqsClientGateway {

	SqsClient getClient();

	String montarQueueUrl(String fila);

	String extrairNomeFila(String queueUrl);
}
