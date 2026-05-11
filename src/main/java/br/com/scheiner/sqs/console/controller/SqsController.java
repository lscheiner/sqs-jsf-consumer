package br.com.scheiner.sqs.console.controller;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

public interface SqsController {
	
	public default void  adicionarMensagem( FacesMessage.Severity severity,  String resumo,  String detalhe) {
        FacesContext.getCurrentInstance().addMessage( null, new FacesMessage(severity,resumo,detalhe));
    }

}
