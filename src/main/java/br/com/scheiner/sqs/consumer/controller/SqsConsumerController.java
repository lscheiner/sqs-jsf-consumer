package br.com.scheiner.sqs.consumer.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;

import br.com.scheiner.sqs.consumer.consumer.SqsConsumerService;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import software.amazon.awssdk.services.sqs.model.Message;

@Named
@ViewScoped
public class SqsConsumerController  {

    private final List<String> filas;

    private final SqsConsumerService sqsConsumerService;

    private String filaSelecionada;

    private Integer quantidadeMensagens = 5;

    private List<Message> mensagens = new ArrayList<>();

    private String conteudoMensagem;

    public SqsConsumerController(
            @Value("${app.sqs.filas}") List<String> filas,
            SqsConsumerService sqsConsumerService) {

        this.filas = filas;
        this.sqsConsumerService = sqsConsumerService;
    }

    public void buscarMensagens() {

        mensagens = sqsConsumerService.consumirMensagens(
                filaSelecionada,
                quantidadeMensagens);
    }

    public void visualizarMensagem(Message mensagem) {

        conteudoMensagem = mensagem.body();
    }

    public List<String> getFilas() {
        return filas;
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