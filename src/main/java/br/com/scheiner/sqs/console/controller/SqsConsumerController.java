package br.com.scheiner.sqs.console.controller;

import java.util.ArrayList;
import java.util.List;

import br.com.scheiner.sqs.console.service.SqsConsumerService;
import br.com.scheiner.sqs.console.service.SqsQueueService;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import software.amazon.awssdk.services.sqs.model.Message;

@Named
@ViewScoped
public class SqsConsumerController  {

    private final SqsConsumerService sqsConsumerService;
    
    private final SqsQueueService sqsQueueService;

    private String filaSelecionada;

    private Integer quantidadeMensagens;

    private List<Message> mensagens;

    private String conteudoMensagem;

    public SqsConsumerController(
    		SqsQueueService sqsQueueService,
            SqsConsumerService sqsConsumerService) {

        this.sqsConsumerService = sqsConsumerService;
        this.mensagens = new ArrayList<>();
        this.quantidadeMensagens = 5 ;
        this.sqsQueueService = sqsQueueService;
    }

    public void buscarMensagens() {
        this.mensagens = this.sqsConsumerService.consumirMensagens(this.filaSelecionada, this.quantidadeMensagens);
    }

    public void visualizarMensagem(Message mensagem) {
        this.conteudoMensagem = mensagem.body();
    }

    public List<String> getFilas() {
        return sqsQueueService.listarFilas();
    }

    public String getFilaSelecionada() {
        return filaSelecionada;
    }

    public void setFilaSelecionada(String filaSelecionada) {
        this.filaSelecionada = filaSelecionada;
    }

    public Integer getQuantidadeMensagens() {
        return quantidadeMensagens;
    }

    public void setQuantidadeMensagens(Integer quantidadeMensagens) {
        this.quantidadeMensagens = quantidadeMensagens;
    }

    public List<Message> getMensagens() {
        return mensagens;
    }

    public String getConteudoMensagem() {
        return conteudoMensagem;
    }
}