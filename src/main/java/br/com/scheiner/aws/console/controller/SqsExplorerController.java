package br.com.scheiner.aws.console.controller;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.scheiner.aws.console.model.SqsExplorerMessage;
import br.com.scheiner.aws.console.model.SqsExplorerQueue;
import br.com.scheiner.aws.console.model.SqsQueueDetails;
import br.com.scheiner.aws.console.service.SqsExplorerService;
import br.com.scheiner.aws.console.utils.JsonUtils;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;

@Named
@ViewScoped
public class SqsExplorerController implements Controller {

	private static final Logger LOGGER = LoggerFactory.getLogger(SqsExplorerController.class);

	private static final int PREVIEW_BODY = 140;

	private static final int WAIT_TIME_SECONDS = 1;
	
	private final SqsExplorerService sqsExplorerService;

	private List<SqsExplorerQueue> filas = new ArrayList<>();

	private String filtroFila;

	private SqsExplorerQueue filaSelecionada;

	private SqsQueueDetails detalhesFila;

	private List<SqsExplorerMessage> mensagens = new ArrayList<>();

	private SqsExplorerMessage mensagemSelecionada;

	private String bodyEnvio;

	private String messageAttributesEnvio;

	private String bodyReplay;

	private Integer intervaloAtualizacao = 0;

	public SqsExplorerController(SqsExplorerService sqsExplorerService) {
		this.sqsExplorerService = sqsExplorerService;
		this.carregarFilas();
	}

	public void carregarFilas() {
		try {
			this.filas = new ArrayList<>(this.sqsExplorerService.listarFilas());
		} catch (Exception e) {
			LOGGER.error("Erro carregando filas no SQS Explorer", e);
			this.filas = new ArrayList<>();
			this.adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Erro", "Erro ao carregar filas SQS.");
		}
	}

	public void selecionarFila(SqsExplorerQueue fila) {
		try {
			this.filaSelecionada = fila;
			this.carregarDetalhesFila();
			this.buscarMensagens();
			this.adicionarMensagem(FacesMessage.SEVERITY_INFO, "Fila selecionada", fila.getNome());
		} catch (Exception e) {
			LOGGER.error("Erro selecionando fila {}", fila.getNome(), e);
			this.adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Erro", "Erro ao selecionar fila.");
		}
	}

	public void atualizarTudo() {
		this.atualizarTudo(WAIT_TIME_SECONDS);
	}
	
	public void atualizarTudo(Integer waitTimeSeconds) {
		this.carregarFilas();

		if (this.filaSelecionada != null) {
			this.carregarDetalhesFila();
			this.buscarMensagens(waitTimeSeconds);
		}
	}

	public void buscarMensagens() {
		this.buscarMensagens(WAIT_TIME_SECONDS);	
	}
	
	public void buscarMensagens(Integer waitTimeSeconds) {
		if (this.filaSelecionada == null) {
			return;
		}

		try {
			this.mensagens = new ArrayList<>(this.sqsExplorerService.buscarMensagens(this.filaSelecionada.getUrl(), waitTimeSeconds));
		} catch (Exception e) {
			LOGGER.error("Erro buscando mensagens da fila {}", this.filaSelecionada.getNome(), e);
			this.mensagens = new ArrayList<>();
			this.adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Erro", "Erro ao buscar mensagens.");
		}
	}

	public void enviarMensagem() {
		if (!this.validarJson(this.bodyEnvio, "Body")) {
			return;
		}
		if (this.messageAttributesEnvio != null
				&& !this.messageAttributesEnvio.isBlank()
				&& !this.validarJson(this.messageAttributesEnvio, "Message Attributes")) {
			return;
		}

		try {
			this.sqsExplorerService.enviarMensagem(
					this.filaSelecionada.getUrl(),
					this.bodyEnvio,
					this.messageAttributesEnvio);
			this.bodyEnvio = null;
			this.messageAttributesEnvio = null;
			this.atualizarTudo(5);
			this.adicionarMensagem(FacesMessage.SEVERITY_INFO, "Mensagem enviada", "Mensagem enviada com sucesso.");
		} catch (Exception e) {
			LOGGER.error("Erro enviando mensagem para fila {}", this.filaSelecionada.getNome(), e);
			this.adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Erro", "Erro ao enviar mensagem.");
		}
	}

	public void visualizarMensagem(SqsExplorerMessage mensagem) {
		this.mensagemSelecionada = mensagem;
		this.bodyReplay = mensagem.getBodyFormatado();
	}

	public void reenviarMensagem() {
		if (!this.validarJson(this.bodyReplay, "Body")) {
			return;
		}

		try {
			this.sqsExplorerService.enviarMensagem(this.filaSelecionada.getUrl(), this.bodyReplay, null);
			this.atualizarTudo();
			this.adicionarMensagem(FacesMessage.SEVERITY_INFO, "Mensagem reenviada", "Mensagem reenviada para a mesma fila.");
		} catch (Exception e) {
			LOGGER.error("Erro reenviando mensagem", e);
			this.adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Erro", "Erro ao reenviar mensagem.");
		}
	}

	public void reenviarParaFilaOriginal(SqsExplorerMessage mensagem) {
		if (this.detalhesFila == null || !this.detalhesFila.isDlqComFilaOriginal()) {
			return;
		}

		try {
			this.sqsExplorerService.enviarMensagem(this.detalhesFila.getFilaOriginalUrl(), mensagem.getBody(), null);
			this.adicionarMensagem(FacesMessage.SEVERITY_INFO, "Mensagem reenviada", "Mensagem reenviada para a fila original.");
		} catch (Exception e) {
			LOGGER.error("Erro reenviando mensagem para fila original", e);
			this.adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Erro", "Erro ao reenviar para fila original.");
		}
	}

	public void excluirMensagem(SqsExplorerMessage mensagem) {
		try {
			this.sqsExplorerService.excluirMensagem(this.filaSelecionada.getUrl(), mensagem.getReceiptHandle());
			this.buscarMensagens();
			this.adicionarMensagem(FacesMessage.SEVERITY_INFO, "Mensagem excluída", "Mensagem removida da fila.");
		} catch (Exception e) {
			LOGGER.error("Erro excluindo mensagem", e);
			this.adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Erro", "Erro ao excluir mensagem.");
		}
	}

	public void purgeFila() {
		try {
			this.sqsExplorerService.purge(this.filaSelecionada.getUrl());
			this.buscarMensagens();
			this.adicionarMensagem(FacesMessage.SEVERITY_INFO, "Fila limpa", "Purge executado com sucesso.");
		} catch (Exception e) {
			LOGGER.error("Erro executando purge", e);
			this.adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Erro", "Erro ao executar purge.");
		}
	}

	public void abrirDlq() {
		if (this.detalhesFila == null || this.detalhesFila.getDlqNome() == null) {
			return;
		}

		var dlqUrl = this.sqsExplorerService.buscarQueueUrlPorNome(this.detalhesFila.getDlqNome());
		this.filas.stream()
				.filter(fila -> fila.getUrl().equals(dlqUrl))
				.findFirst()
				.ifPresentOrElse(this::selecionarFila, () -> this.selecionarFila(new SqsExplorerQueue(
						this.detalhesFila.getDlqNome(),
						dlqUrl,
						"Standard",
						0,
						true)));
	}

	public String previewBody(String body) {
		if (body == null || body.isBlank()) {
			return "";
		}

		var normalizado = body.replaceAll("\\s+", " ").trim();
		return normalizado.length() <= PREVIEW_BODY
				? normalizado
				: normalizado.substring(0, PREVIEW_BODY).concat("...");
	}

	public StreamedContent getDownloadMensagem() {
		if (this.mensagemSelecionada == null) {
			return null;
		}

		var conteudo = JsonUtils.prettyPrint(this.mensagemSelecionada.getBody());
		var nomeArquivo = "%s.json".formatted(this.mensagemSelecionada.getMessageId());
		var bytes = conteudo.getBytes(StandardCharsets.UTF_8);

		return DefaultStreamedContent.builder()
				.name(nomeArquivo)
				.contentType("application/json")
				.stream(() -> new ByteArrayInputStream(bytes))
				.build();
	}

	private void carregarDetalhesFila() {
		this.detalhesFila = this.sqsExplorerService.buscarDetalhes(this.filaSelecionada.getUrl());
	}

	private boolean validarJson(String conteudo, String campo) {
		if (conteudo == null || conteudo.isBlank()) {
			this.adicionarMensagem(FacesMessage.SEVERITY_ERROR, "JSON obrigatório", "Informe o conteúdo do campo %s.".formatted(campo));
			return false;
		}
		if (!this.sqsExplorerService.isJsonValido(conteudo)) {
			this.adicionarMensagem(FacesMessage.SEVERITY_ERROR, "JSON inválido", "O campo %s não contém um JSON válido.".formatted(campo));
			return false;
		}

		return true;
	}

	public List<SqsExplorerQueue> getFilasFiltradas() {
		if (this.filtroFila == null || this.filtroFila.isBlank()) {
			return this.filas;
		}

		var filtro = this.filtroFila.toLowerCase();
		return this.filas.stream()
				.filter(fila -> fila.getNome().toLowerCase().contains(filtro))
				.toList();
	}

	public boolean isPossuiFilaSelecionada() {
		return this.filaSelecionada != null;
	}

	public boolean isAtualizacaoAutomaticaHabilitada() {
		return this.intervaloAtualizacao != null && this.intervaloAtualizacao > 0;
	}

	public List<SqsExplorerMessage> getMensagens() {
		return this.mensagens;
	}

	public SqsExplorerQueue getFilaSelecionada() {
		return this.filaSelecionada;
	}

	public SqsQueueDetails getDetalhesFila() {
		return this.detalhesFila;
	}

	public String getFiltroFila() {
		return this.filtroFila;
	}

	public void setFiltroFila(String filtroFila) {
		this.filtroFila = filtroFila;
	}

	public String getBodyEnvio() {
		return this.bodyEnvio;
	}

	public void setBodyEnvio(String bodyEnvio) {
		this.bodyEnvio = bodyEnvio;
	}

	public String getMessageAttributesEnvio() {
		return this.messageAttributesEnvio;
	}

	public void setMessageAttributesEnvio(String messageAttributesEnvio) {
		this.messageAttributesEnvio = messageAttributesEnvio;
	}

	public SqsExplorerMessage getMensagemSelecionada() {
		return this.mensagemSelecionada;
	}

	public Map<String, String> getMensagemSelecionadaAttributes() {
		return this.mensagemSelecionada != null ? this.mensagemSelecionada.getAttributes() : Map.of();
	}

	public Map<String, MessageAttributeValue> getMensagemSelecionadaMessageAttributes() {
		return this.mensagemSelecionada != null ? this.mensagemSelecionada.getMessageAttributes() : Map.of();
	}

	public String getBodyReplay() {
		return this.bodyReplay;
	}

	public void setBodyReplay(String bodyReplay) {
		this.bodyReplay = bodyReplay;
	}

	public Integer getIntervaloAtualizacao() {
		return this.intervaloAtualizacao;
	}

	public void setIntervaloAtualizacao(Integer intervaloAtualizacao) {
		this.intervaloAtualizacao = intervaloAtualizacao;
	}
}
