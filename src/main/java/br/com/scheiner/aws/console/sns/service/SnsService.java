package br.com.scheiner.aws.console.sns.service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.scheiner.aws.console.sns.gateway.SnsClientGateway;
import br.com.scheiner.aws.console.sns.model.SnsSubscription;
import br.com.scheiner.aws.console.sns.model.SnsTopic;
import software.amazon.awssdk.services.sns.model.ListTopicsRequest;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.Topic;

@Service
public class SnsService {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private static final String CONTENT_TYPE = "content-type";

	private static final String APPLICATION_JSON = "application/json";

	private final SnsClientGateway snsClientGateway;

	public SnsService(SnsClientGateway snsClientGateway) {
		this.snsClientGateway = snsClientGateway;
	}

	public List<SnsTopic> listarTopicos() {
		return this.snsClientGateway.getClient()
				.listTopicsPaginator(ListTopicsRequest.builder().build())
				.topics()
				.stream()
				.map(Topic::topicArn)
				.map(topicArn -> new SnsTopic(
						this.extrairNomeTopico(topicArn),
						topicArn,
						this.listarAssinaturas(topicArn).size()))
				.sorted(Comparator.comparing(SnsTopic::getNome, String.CASE_INSENSITIVE_ORDER))
				.toList();
	}

	public List<SnsSubscription> listarAssinaturas(String topicArn) {
		return this.snsClientGateway.getClient()
				.listSubscriptionsByTopicPaginator(builder -> builder.topicArn(topicArn))
				.subscriptions()
				.stream()
				.map(subscription -> new SnsSubscription(
						subscription.subscriptionArn(),
						subscription.protocol(),
						subscription.endpoint()))
				.toList();
	}

	public void publicarMensagem(String topicArn, String subject, String message, String messageAttributesJson) {
		var request = PublishRequest.builder()
				.topicArn(topicArn)
				.subject(StringUtils.hasText(subject) ? subject : null)
				.message(message)
				.messageAttributes(this.converterMessageAttributes(messageAttributesJson))
				.build();

		this.snsClientGateway.getClient().publish(request);
	}

	public boolean isJsonValido(String conteudo) {
		try {
			OBJECT_MAPPER.readTree(conteudo);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public String extrairNomeTopico(String topicArn) {
		if (topicArn == null || topicArn.isBlank()) {
			return "";
		}
		return topicArn.substring(topicArn.lastIndexOf(':') + 1);
	}

	private Map<String, MessageAttributeValue> converterMessageAttributes(String json) {
		if (!StringUtils.hasText(json)) {
			return this.montarAttributesPadrao();
		}

		try {
			var node = OBJECT_MAPPER.readTree(json);
			var attributes = new HashMap<String, MessageAttributeValue>();

			node.properties().forEach(entry -> attributes.put(entry.getKey(), this.converterMessageAttribute(entry.getValue())));

			return attributes;
		} catch (Exception e) {
			throw new IllegalArgumentException("Message Attributes deve ser um JSON valido.", e);
		}
	}

	private Map<String, MessageAttributeValue> montarAttributesPadrao() {
		return Map.of(
				CONTENT_TYPE,
				MessageAttributeValue.builder()
						.dataType("String")
						.stringValue(APPLICATION_JSON)
						.build());
	}

	private MessageAttributeValue converterMessageAttribute(JsonNode node) {
		var dataType = node.path("dataType").asText("String");
		var stringValue = node.path("stringValue").asText();

		return MessageAttributeValue.builder()
				.dataType(dataType)
				.stringValue(stringValue)
				.build();
	}
}
