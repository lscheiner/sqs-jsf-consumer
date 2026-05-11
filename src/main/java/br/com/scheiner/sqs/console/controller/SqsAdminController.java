package br.com.scheiner.sqs.console.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.scheiner.sqs.console.config.SqsClientProvider;
import br.com.scheiner.sqs.console.service.SqsQueueService;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import software.amazon.awssdk.regions.Region;

@Named
@ViewScoped
public class SqsAdminController implements SqsController {

	private static final Logger LOGGER = LoggerFactory.getLogger(SqsAdminController.class);

	private final SqsClientProvider sqsClientProvider;

	private final SqsQueueService sqsQueueService;

	private String endpoint;

	private String region;

	private boolean connected;

	private String filaSelecionada;

	public SqsAdminController(SqsClientProvider sqsClientProvider, SqsQueueService sqsQueueService) {

		this.sqsClientProvider = sqsClientProvider;
		this.sqsQueueService = sqsQueueService;

		this.endpoint = sqsClientProvider.getEndpoint();
		this.region = sqsClientProvider.getRegion().id();

		this.testarConexao();
	}

	public void aplicarConfiguracao() {

		try {

			this.sqsClientProvider.reconfigurar(endpoint, Region.of(region));
			this.connected = true;
			this.testarConexao();

		} catch (Exception ex) {
			this.connected = false;
		}
	}

	public void testarConexao() {

		try {
			this.sqsClientProvider.getClient().listQueues();
			this.connected = true;
			adicionarMensagem(FacesMessage.SEVERITY_INFO, "Conexão OK", "Conexão realizada com sucesso.");

		} catch (Exception ex) {
			connected = false;
			adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Erro de conexão", ex.getMessage());
		}
	}

	public void purgeFila() {

		try {

			this.sqsQueueService.purgeFila(this.filaSelecionada);

			adicionarMensagem(FacesMessage.SEVERITY_INFO, "Fila limpa", "Purge executado com sucesso.");

		} catch (Exception ex) {

			adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Erro ao executar purge", ex.getMessage());
		}
	}

	public List<String> getFilas() {

		try {
			return sqsQueueService.listarFilas();
		} catch (Exception e) {
			LOGGER.error("Erro carregando filas", e);
		}
		return List.of();
	}

	public List<String> getRegions() {

		return Region.regions().stream().map(Region::id).sorted().toList();
	}

	public String getEndpointAtual() {
		return sqsClientProvider.getEndpoint();
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	public String getFilaSelecionada() {
		return filaSelecionada;
	}

	public void setFilaSelecionada(String filaSelecionada) {
		this.filaSelecionada = filaSelecionada;
	}
}