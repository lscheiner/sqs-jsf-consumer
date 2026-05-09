package br.com.scheiner.sqs.consumer.controller;

import java.io.Serializable;
import java.util.List;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

@Named
@ViewScoped
public class SqsController implements Serializable {

    private static final long serialVersionUID = 1L;

    private String payload;

    private String filaSelecionada;

    private final List<String> filas = List.of(
            "pedido-criado",
            "pedido-cancelado",
            "cliente-atualizado",
            "envio-email",
            "geracao-boleto");

    public void enviarMensagem() {

        System.out.println("Fila selecionada: " + filaSelecionada);
        System.out.println("Payload enviado:");
        System.out.println(payload);

        FacesContext.getCurrentInstance()
                .addMessage(null,
                        new FacesMessage(
                                FacesMessage.SEVERITY_INFO,
                                "Sucesso",
                                "Mensagem enviada para fila SQS."));
    }

    public void limpar() {
        payload = null;
        filaSelecionada = null;
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

    public List<String> getFilas() {
        return filas;
    }
}