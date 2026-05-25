package br.com.scheiner.aws.console.sqs;

import org.springframework.stereotype.Component;

import br.com.scheiner.aws.console.config.AwsConfiguration;

@Component
public class SqsQueueUrlResolver {

	private static final String ACCOUNT_ID = "000000000000";

	private final AwsConfiguration awsConfiguration;

	public SqsQueueUrlResolver(AwsConfiguration awsConfiguration) {
		this.awsConfiguration = awsConfiguration;
	}

	public String montarQueueUrl(String fila) {
		return "%s/%s/%s".formatted(
				this.awsConfiguration.getEndpoint(),
				ACCOUNT_ID,
				fila);
	}

	public String extrairNomeFila(String queueUrl) {
		return queueUrl.substring(queueUrl.lastIndexOf("/") + 1);
	}
}
