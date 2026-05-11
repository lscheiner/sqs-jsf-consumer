package br.com.scheiner.sqs.console.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.scheiner.sqs.console.service.SqsProducerService;
import br.com.scheiner.sqs.console.service.SqsQueueService;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

@Named
@ViewScoped
public class SqsProducerController implements SqsController  {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqsProducerController.class);

    private final SqsProducerService sqsProducerService;
    
    private final SqsQueueService sqsQueueService;

    private String payload;

    private String filaSelecionada;

    public SqsProducerController(
    		SqsQueueService sqsQueueService,
            SqsProducerService sqsProducerService) {

        this.sqsProducerService = sqsProducerService;
        this.sqsQueueService = sqsQueueService;

    }

    public void enviarMensagem() {

        LOGGER.info("Enviando mensagem para fila [{}]",  filaSelecionada);

        LOGGER.info("Payload enviado: {}", payload);

        sqsProducerService.enviarMensagem( filaSelecionada, payload);
        
        LOGGER.info("Mensagem enviada com sucesso para fila [{}]", filaSelecionada);
        
        adicionarMensagem(FacesMessage.SEVERITY_INFO,"Sucesso","Mensagem enviada para fila SQS.");
    }

    public void limpar() {

        payload = null;
        filaSelecionada = null;
    }

	public List<String> getFilas() {

		try {
			return sqsQueueService.listarFilas();
		} catch (Exception e) {
			LOGGER.error("Erro carregando filas", e);
		}
		return List.of();
	}

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getFilaSelecionada() {
        return filaSelecionada;
    }

    public void setFilaSelecionada(String filaSelecionada) {
        this.filaSelecionada = filaSelecionada;
    }
}