package br.com.scheiner.aws.console.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.scheiner.aws.console.service.SqsConsumerService;
import br.com.scheiner.aws.console.service.SqsQueueService;
import br.com.scheiner.aws.console.utils.JsonUtils;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.MessageSystemAttributeName;

@Named
@ViewScoped
public class SqsConsumerController  {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqsConsumerController.class);
	
    private static final int PAYLOAD_PREVIEW_LENGTH = 120;
    
    private final SqsConsumerService sqsConsumerService;
    
    private final SqsQueueService sqsQueueService;

    private String filaSelecionada;

    private Integer quantidadeMensagens;

    private List<Message> mensagens;

    private String conteudoMensagem;
    
    private Map<String, MessageAttributeValue> messageAttributes; 

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
        this.conteudoMensagem = JsonUtils.prettyPrint(mensagem.body());
        this.messageAttributes = mensagem.messageAttributes();
    }


    public String payloadPreview(Message mensagem) {

        return Optional.ofNullable(mensagem.body())
                .map(body -> body.replaceAll("\\s+", " ").trim())
                .filter(body -> !body.isBlank())
                .map(body -> body.length() > PAYLOAD_PREVIEW_LENGTH
                        ? body.substring(0, PAYLOAD_PREVIEW_LENGTH).concat("...")
                        : body)
                .orElse("");
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
    public Map<String, MessageAttributeValue> getMessageAttributes() {
        return messageAttributes;
    }
}