package br.com.scheiner.sqs.console.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.scheiner.sqs.console.service.SqsConsumerService;
import br.com.scheiner.sqs.console.service.SqsQueueService;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.MessageSystemAttributeName;

@Named
@ViewScoped
public class SqsConsumerController  {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqsConsumerController.class);
	
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
    
    public String receiveCount(Message message) {
        return message.attributes().get(MessageSystemAttributeName.APPROXIMATE_RECEIVE_COUNT);
    }

    public void visualizarMensagem(Message mensagem) {
        this.conteudoMensagem = mensagem.body();
    }

	public List<String> getFilas() {

		try {
			return sqsQueueService.listarFilas();
		} catch (Exception e) {
			LOGGER.error("Erro carregando filas", e);
		}
		return List.of();
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