package br.com.scheiner.aws.console.service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.scheiner.aws.console.model.SqsExplorerMessage;
import br.com.scheiner.aws.console.model.SqsExplorerQueue;
import br.com.scheiner.aws.console.model.SqsQueueDetails;
import br.com.scheiner.aws.console.sqs.SqsClientGateway;
import br.com.scheiner.aws.console.utils.JsonUtils;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest;
import software.amazon.awssdk.services.sqs.model.ListQueuesRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.MessageSystemAttributeName;
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Service
public class SqsExplorerService {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private static final int MAX_MENSAGENS_RECEIVE = 10;

	private static final int VISIBILITY_TIMEOUT_EXPLORER = 2;

	private static final String FIFO = "FIFO";

	private static final String STANDARD = "Standard";

	private static final String CONTENT_TYPE = "content-type";

	private static final String APPLICATION_JSON = "application/json";

	private final SqsClientGateway sqsClientGateway;

	public SqsExplorerService(SqsClientGateway sqsClientGateway) {
		this.sqsClientGateway = sqsClientGateway;
	}

	public List<SqsExplorerQueue> listarFilas() {
		
		var urls = this.sqsClientGateway.getClient()
				.listQueues(ListQueuesRequest.builder().build())
				.queueUrls();
		
		var atributosPorUrl = new LinkedHashMap<String, Map<QueueAttributeName, String>>();
		var dlqArns = new HashSet<String>();

		urls.forEach(url -> {
			var atributos = this.buscarAtributos(url);
			atributosPorUrl.put(url, atributos);
			this.extrairDlqArn(atributos).ifPresent(dlqArns::add);
		});

		return urls.stream()
				.map(url -> this.criarQueueResumo(url, atributosPorUrl.get(url), dlqArns))
				.sorted(Comparator.comparing(
				        SqsExplorerQueue::getNome,
				        String.CASE_INSENSITIVE_ORDER))
				.toList();
	}

	public SqsQueueDetails buscarDetalhes(String queueUrl) {
		
		var detalhes = new SqsQueueDetails();
		var atributos = this.buscarAtributos(queueUrl);
		var dlqArn = this.extrairDlqArn(atributos).orElse(null);

		detalhes.setNome(this.sqsClientGateway.extrairNomeFila(queueUrl));
		detalhes.setUrl(queueUrl);
		detalhes.setArn(atributos.get(QueueAttributeName.QUEUE_ARN));
		detalhes.setTipo(this.tipoFila(queueUrl, atributos));
		detalhes.setVisibilityTimeout(atributos.get(QueueAttributeName.VISIBILITY_TIMEOUT));
		detalhes.setMessageRetentionPeriod(atributos.get(QueueAttributeName.MESSAGE_RETENTION_PERIOD));
		detalhes.setReceiveMessageWaitTime(atributos.get(QueueAttributeName.RECEIVE_MESSAGE_WAIT_TIME_SECONDS));
		detalhes.setDlqArn(dlqArn);
		detalhes.setDlqNome(this.nomeFilaPorArn(dlqArn));
		detalhes.setMaxReceiveCount(this.extrairMaxReceiveCount(atributos).orElse(null));

		this.buscarFilaOriginal(queueUrl).ifPresent(original -> {
			detalhes.setFilaOriginalUrl(original);
			detalhes.setFilaOriginalNome(this.sqsClientGateway.extrairNomeFila(original));
		});

		return detalhes;
	}

	public List<SqsExplorerMessage> buscarMensagens(String queueUrl , Integer waitTimeSeconds) {
		
		var response = this.sqsClientGateway.getClient().receiveMessage(
				ReceiveMessageRequest.builder()
						.queueUrl(queueUrl)
						.maxNumberOfMessages(MAX_MENSAGENS_RECEIVE)
						.visibilityTimeout(VISIBILITY_TIMEOUT_EXPLORER)
						.waitTimeSeconds(waitTimeSeconds)
						.attributeNames(
                                QueueAttributeName.ALL
                         )
						.messageAttributeNames("All")
						.build());

		return response.messages()
				.stream()
				.map(this::criarMensagem)
				.toList();
	}

	public void enviarMensagem(String queueUrl, String body, String messageAttributesJson) {
		var request = SendMessageRequest.builder()
				.queueUrl(queueUrl)
				.messageBody(body)
				.messageAttributes(this.converterMessageAttributes(messageAttributesJson))
				.build();

		this.sqsClientGateway.getClient().sendMessage(request);
	}

	public void excluirMensagem(String queueUrl, String receiptHandle) {
		this.sqsClientGateway.getClient().deleteMessage(
				DeleteMessageRequest.builder()
						.queueUrl(queueUrl)
						.receiptHandle(receiptHandle)
						.build());
	}

	public void purge(String queueUrl) {
		this.sqsClientGateway.getClient().purgeQueue(
				PurgeQueueRequest.builder()
						.queueUrl(queueUrl)
						.build());
	}

	public String buscarQueueUrlPorNome(String nomeFila) {
		return this.sqsClientGateway.montarQueueUrl(nomeFila);
	}

	public boolean isJsonValido(String conteudo) {
		try {
			OBJECT_MAPPER.readTree(conteudo);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private Map<QueueAttributeName, String> buscarAtributos(String queueUrl) {
		return this.sqsClientGateway.getClient()
				.getQueueAttributes(
						GetQueueAttributesRequest.builder()
								.queueUrl(queueUrl)
								.attributeNames(QueueAttributeName.ALL)
								.build())
				.attributes();
	}

	private SqsExplorerQueue criarQueueResumo(
			String queueUrl,
			Map<QueueAttributeName, String> atributos,
			java.util.Set<String> dlqArns) {

		var nome = this.sqsClientGateway.extrairNomeFila(queueUrl);
		var arn = atributos.get(QueueAttributeName.QUEUE_ARN);
		var quantidade = this.converterInteiro(atributos.get(QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES));
		var dlq = dlqArns.contains(arn) || nome.toLowerCase().contains("dlq");

		return new SqsExplorerQueue(nome, queueUrl, this.tipoFila(queueUrl, atributos), quantidade, dlq);
	}

	private SqsExplorerMessage criarMensagem(Message message) {
		
		var mensagem = new SqsExplorerMessage();
		var attributes = new LinkedHashMap<>(message.attributesAsStrings());

		mensagem.setMessageId(message.messageId());
		mensagem.setReceiptHandle(message.receiptHandle());
		mensagem.setBody(message.body());
		mensagem.setBodyFormatado(JsonUtils.prettyPrint(message.body()));
		mensagem.setSentTimestamp(attributes.get(MessageSystemAttributeName.SENT_TIMESTAMP.toString()));
		mensagem.setReceiveCount(attributes.get(MessageSystemAttributeName.APPROXIMATE_RECEIVE_COUNT.toString()));
		mensagem.setAttributes(attributes);
		mensagem.setMessageAttributes(message.messageAttributes());

		return mensagem;
	}

	private Map<String, MessageAttributeValue> converterMessageAttributes(String json) {
		if (json == null || json.isBlank()) {
			return this.montarAttributesPadrao();
		}

		try {
			var node = OBJECT_MAPPER.readTree(json);
			var attributes = new HashMap<String, MessageAttributeValue>();

			node.properties().forEach(entry -> attributes.put(entry.getKey(), this.converterMessageAttribute(entry.getValue())));

			return attributes;
		} catch (Exception e) {
			throw new IllegalArgumentException("Message Attributes deve ser um JSON válido.", e);
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

	private java.util.Optional<String> extrairDlqArn(Map<QueueAttributeName, String> atributos) {
		return this.extrairRedrivePolicy(atributos)
				.map(policy -> policy.path("deadLetterTargetArn").asText(null))
				.filter(Objects::nonNull);
	}

	private java.util.Optional<String> extrairMaxReceiveCount(Map<QueueAttributeName, String> atributos) {
		return this.extrairRedrivePolicy(atributos)
				.map(policy -> policy.path("maxReceiveCount").asText(null))
				.filter(Objects::nonNull);
	}

	private java.util.Optional<JsonNode> extrairRedrivePolicy(Map<QueueAttributeName, String> atributos) {
		var redrivePolicy = atributos.get(QueueAttributeName.REDRIVE_POLICY);

		if (redrivePolicy == null || redrivePolicy.isBlank()) {
			return java.util.Optional.empty();
		}

		try {
			return java.util.Optional.of(OBJECT_MAPPER.readTree(redrivePolicy));
		} catch (Exception e) {
			return java.util.Optional.empty();
		}
	}

	private java.util.Optional<String> buscarFilaOriginal(String dlqUrl) {
		var dlqArn = this.buscarAtributos(dlqUrl).get(QueueAttributeName.QUEUE_ARN);

		if (dlqArn == null) {
			return java.util.Optional.empty();
		}

		return this.sqsClientGateway.getClient()
				.listQueues()
				.queueUrls()
				.stream()
				.filter(url -> this.extrairDlqArn(this.buscarAtributos(url)).filter(dlqArn::equals).isPresent())
				.findFirst();
	}

	private String nomeFilaPorArn(String arn) {
		if (arn == null || arn.isBlank()) {
			return null;
		}

		return arn.substring(arn.lastIndexOf(':') + 1);
	}

	private String tipoFila(String queueUrl, Map<QueueAttributeName, String> atributos) {
		var fifo = atributos.get(QueueAttributeName.FIFO_QUEUE);
		return Boolean.parseBoolean(fifo) || queueUrl.endsWith(".fifo") ? FIFO : STANDARD;
	}

	private Integer converterInteiro(String valor) {
		try {
			return Integer.valueOf(valor);
		} catch (Exception e) {
			return 0;
		}
	}
}
