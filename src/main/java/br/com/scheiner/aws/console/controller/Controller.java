package br.com.scheiner.aws.console.controller;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

public interface Controller {
	
	public default void  adicionarMensagem( FacesMessage.Severity severity,  String resumo,  String detalhe) {
        FacesContext.getCurrentInstance().addMessage( null, new FacesMessage(severity,resumo,detalhe));
    }

}
