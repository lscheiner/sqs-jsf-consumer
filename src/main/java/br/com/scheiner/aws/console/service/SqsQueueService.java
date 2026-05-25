package br.com.scheiner.aws.console.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.scheiner.aws.console.sqs.SqsClientGateway;
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest;

@Service
public class SqsQueueService {

    private final SqsClientGateway sqsClientGateway;

    public SqsQueueService(SqsClientGateway sqsClientGateway) {
        this.sqsClientGateway = sqsClientGateway;
    }

    public List<String> listarFilas() {

        return this.sqsClientGateway.getClient().listQueues().queueUrls()
                .stream()
                .map(this.sqsClientGateway::extrairNomeFila)
                .sorted()
                .toList();
    }
    
    public void purgeFila(String fila) {

    	 final var request = PurgeQueueRequest.builder()
                 .queueUrl(this.sqsClientGateway.montarQueueUrl(fila))
                 .build();
    	 
         this.sqsClientGateway.getClient().purgeQueue(request);
    }

   
}
