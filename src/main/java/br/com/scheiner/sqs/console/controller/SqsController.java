package br.com.scheiner.sqs.console.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import br.com.scheiner.sqs.console.producer.SqsProducerService;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

@Named
@ViewScoped
public class SqsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqsController.class);

    private final List<String> filas;

    private final SqsProducerService sqsProducerService;

    private String payload;

    private String filaSelecionada;

    public SqsController(
            @Value("${app.sqs.filas}") List<String> filas,
            SqsProducerService sqsProducerService) {

        this.filas = filas;
        this.sqsProducerService = sqsProducerService;

        LOGGER.info("SqsController inicializada com {} filas configuradas",  filas.size());
    }

    public void enviarMensagem() {

        LOGGER.info("Enviando mensagem para fila [{}]",  filaSelecionada);

        LOGGER.info("Payload enviado: {}", payload);

        sqsProducerService.enviarMensagem( filaSelecionada, payload);
        
        LOGGER.info("Mensagem enviada com sucesso para fila [{}]", filaSelecionada);

        FacesContext.getCurrentInstance().addMessage(null,new FacesMessage(FacesMessage.SEVERITY_INFO,"Sucesso","Mensagem enviada para fila SQS."));
    }

    public void limpar() {

        payload = null;
        filaSelecionada = null;
    }

    public List<String> getFilas() {
        return filas;
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