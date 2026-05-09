package br.com.scheiner.sqs.console.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.scheiner.sqs.console.config.SqsClientProvider;

@Service
public class SqsQueueService {

    private final SqsClientProvider sqsClientProvider;

    public SqsQueueService(SqsClientProvider sqsClientProvider) {
        this.sqsClientProvider = sqsClientProvider;
    }

    public List<String> listarFilas() {

        return sqsClientProvider.getClient().listQueues().queueUrls()
                .stream()
                .map(this::extrairNomeFila)
                .sorted()
                .toList();
    }

    private String extrairNomeFila(String queueUrl) {
        return queueUrl.substring(queueUrl.lastIndexOf("/") + 1);
    }
}