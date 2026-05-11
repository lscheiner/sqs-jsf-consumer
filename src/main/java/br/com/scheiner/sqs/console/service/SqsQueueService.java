package br.com.scheiner.sqs.console.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.scheiner.sqs.console.config.SqsClientProvider;
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest;

@Service
public class SqsQueueService {

    private final SqsClientProvider sqsClientProvider;

    public SqsQueueService(SqsClientProvider sqsClientProvider) {
        this.sqsClientProvider = sqsClientProvider;
    }

    public List<String> listarFilas() {

        return sqsClientProvider.getClient().listQueues().queueUrls()
                .stream()
                .map(sqsClientProvider::extrairNomeFila)
                .sorted()
                .toList();
    }
    
    public void purgeFila(String fila) {

    	 final var request = PurgeQueueRequest.builder()
                 .queueUrl(sqsClientProvider.montarQueueUrl(fila))
                 .build();
    	 
         this.sqsClientProvider.getClient().purgeQueue(request);
    }

   
}