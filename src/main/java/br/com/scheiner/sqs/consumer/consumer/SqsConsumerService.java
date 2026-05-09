package br.com.scheiner.sqs.consumer.consumer;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

@Service
public class SqsConsumerService {

    private static final String ACCOUNT_ID = "000000000000";

    private final SqsClient sqsClient;

    private final String endpoint;

    public SqsConsumerService(
            SqsClient sqsClient,
            @Value("${aws.sqs.endpoint}") String endpoint) {

        this.sqsClient = sqsClient;
        this.endpoint = endpoint;
    }

    public List<Message> consumirMensagens(String fila,
                                           Integer quantidadeMensagens) {

        ReceiveMessageRequest request =
                ReceiveMessageRequest.builder()
                        .queueUrl(montarQueueUrl(fila))
                        .maxNumberOfMessages(quantidadeMensagens)
                        .visibilityTimeout(1)
                        .waitTimeSeconds(1)
                        .build();

        return sqsClient.receiveMessage(request).messages();
    }

    private String montarQueueUrl(String fila) {
        return "%s/%s/%s".formatted(endpoint,ACCOUNT_ID, fila);
    }
}