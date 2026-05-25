package br.com.scheiner.aws.console.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import br.com.scheiner.aws.console.sqs.SqsClientGateway;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Service
public class SqsProducerService {

    private final SqsClientGateway sqsClientGateway;

    public SqsProducerService(SqsClientGateway sqsClientGateway) {
        this.sqsClientGateway = sqsClientGateway;
    }

    public void enviarMensagem(String fila, String payload) {

        final var request =
                SendMessageRequest.builder()
                        .queueUrl(this.sqsClientGateway.montarQueueUrl(fila))
                        .messageBody(payload)
                        .messageAttributes(this.montarAttributes())
                        .build();

        this.sqsClientGateway.getClient().sendMessage(request);
    }

    private Map<String, MessageAttributeValue> montarAttributes() {

        return Map.of(
                "content-type",
                MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue("application/json")
                        .build());
    }


}
