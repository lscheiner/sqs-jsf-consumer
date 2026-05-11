package br.com.scheiner.sqs.console.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.scheiner.sqs.console.config.SqsClientProvider;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.MessageSystemAttributeName;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

@Service
public class SqsConsumerService {

    private final SqsClientProvider sqsClientProvider;

    public SqsConsumerService(SqsClientProvider sqsClientProvider) {

        this.sqsClientProvider = sqsClientProvider;
    }

    public List<Message> consumirMensagens(String fila, Integer quantidadeMensagens) {

        var request =
                ReceiveMessageRequest.builder()
                        .queueUrl(this.sqsClientProvider.montarQueueUrl(fila))
                        .maxNumberOfMessages(quantidadeMensagens)
                        .visibilityTimeout(1)
                        .waitTimeSeconds(1)
                        .messageSystemAttributeNames(
                                MessageSystemAttributeName.APPROXIMATE_RECEIVE_COUNT
                         )
                        .messageAttributeNames("All")
                        .build();

        return this.sqsClientProvider.getClient().receiveMessage(request).messages();
    }
}