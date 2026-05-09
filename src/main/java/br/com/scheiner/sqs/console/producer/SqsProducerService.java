package br.com.scheiner.sqs.console.producer;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Service
public class SqsProducerService {

    private static final String ACCOUNT_ID = "000000000000";
    private final SqsClient sqsClient;
    private final String endpoint;

    public SqsProducerService(
            SqsClient sqsClient,
            @Value("${aws.sqs.endpoint}") String endpoint) {

        this.sqsClient = sqsClient;
        this.endpoint = endpoint;
    }

    public void enviarMensagem(String fila, String payload) {

        final var request =
                SendMessageRequest.builder()
                        .queueUrl(montarQueueUrl(fila))
                        .messageBody(payload)
                        .messageAttributes(montarAttributes())
                        .build();

        this.sqsClient.sendMessage(request);
    }

    private Map<String, MessageAttributeValue> montarAttributes() {

        return Map.of(
                "content-type",
                MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue("application/json")
                        .build());
    }

    private String montarQueueUrl(String fila) {

        return "%s/%s/%s".formatted(
                endpoint,
                ACCOUNT_ID,
                fila);
    }
}