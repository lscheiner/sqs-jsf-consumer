package br.com.scheiner.aws.console.sqs.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.sqs.SqsClient;

@Service
public class SqsHealthService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SqsHealthService.class);

	public boolean isConectado(SqsClient sqsClient) {
		try {
			sqsClient.listQueues();
			return true;
		} catch (Exception ex) {
			LOGGER.error("Erro conectando no SQS", ex);
			return false;
		}
	}
}
