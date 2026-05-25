package br.com.scheiner.aws.console.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.scheiner.aws.console.sqs.SqsClientGateway;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

@Service
public class SqsConsumerService {

    private final SqsClientGateway sqsClientGateway;

    public SqsConsumerService(SqsClientGateway sqsClientGateway) {

        this.sqsClientGateway = sqsClientGateway;
    }

    public List<Message> consumirMensagens(String fila, Integer quantidadeMensagens) {

        var request =
                ReceiveMessageRequest.builder()
                        .queueUrl(this.sqsClientGateway.montarQueueUrl(fila))
                        .maxNumberOfMessages(quantidadeMensagens)
                        .visibilityTimeout(1)
                        .waitTimeSeconds(5)
                        .attributeNames(
                                QueueAttributeName.ALL
                         )
                        .messageAttributeNames("All")
                        .build();

        return this.sqsClientGateway.getClient().receiveMessage(request).messages();
    }
}
