package br.com.scheiner.aws.console.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.scheiner.aws.console.service.SqsQueueService;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

@Named
@ViewScoped
public class SqsAdminController implements SqsController {

	private static final Logger LOGGER = LoggerFactory.getLogger(SqsAdminController.class);

	private final SqsQueueService sqsQueueService;

	private String filaSelecionada;

	public SqsAdminController( SqsQueueService sqsQueueService) {
		this.sqsQueueService = sqsQueueService;
	}

	public void purgeFila() {

		try {

			this.sqsQueueService.purgeFila(this.filaSelecionada);
			this.adicionarMensagem(FacesMessage.SEVERITY_INFO, "Fila limpa", "Purge executado com sucesso.");

		} catch (Exception ex) {

			this.adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Erro ao executar purge", ex.getMessage());
		}
	}

	public List<String> getFilas() {

		try {
			return this.sqsQueueService.listarFilas();
		} catch (Exception e) {
			LOGGER.error("Erro carregando filas", e);
		}
		return List.of();
	}

	public String getFilaSelecionada() {
		return this.filaSelecionada;
	}

	public void setFilaSelecionada(String filaSelecionada) {
		this.filaSelecionada = filaSelecionada;
	}

	public SqsQueueService getSqsQueueService() {
		return this.sqsQueueService;
	}
	
}
