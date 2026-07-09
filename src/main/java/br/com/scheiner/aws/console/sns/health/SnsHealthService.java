package br.com.scheiner.aws.console.sns.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import br.com.scheiner.aws.console.sns.gateway.SnsClientGateway;
import software.amazon.awssdk.services.sns.model.ListTopicsRequest;

@Service
public class SnsHealthService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SnsHealthService.class);

	public boolean isConectado(SnsClientGateway snsClientGateway) {
		try {
			snsClientGateway.getClient().listTopics(
					ListTopicsRequest.builder()
							.build());
			return true;
		} catch (Exception ex) {
			LOGGER.error("Erro conectando no SNS", ex);
			return false;
		}
	}
}
