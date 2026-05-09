package br.com.scheiner.sqs.console.service;

import java.util.List;

import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.sqs.SqsClient;

@Service
public class SqsQueueService {

    private final SqsClient sqsClient;

    public SqsQueueService(SqsClient sqsClient) {
        this.sqsClient = sqsClient;
    }

    public List<String> listarFilas() {

        return sqsClient.listQueues().queueUrls()
                .stream()
                .map(this::extrairNomeFila)
                .sorted()
                .toList();
    }

    private String extrairNomeFila(String queueUrl) {
        return queueUrl.substring(queueUrl.lastIndexOf("/") + 1);
    }
}