package br.com.scheiner.aws.console.sns.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.scheiner.aws.console.controller.Controller;
import br.com.scheiner.aws.console.sns.model.SnsSubscription;
import br.com.scheiner.aws.console.sns.model.SnsTopic;
import br.com.scheiner.aws.console.sns.service.SnsService;
import br.com.scheiner.aws.console.web.navigation.ApplicationRoute;
import br.com.scheiner.aws.console.web.navigation.NavigationManager;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

@Named
@ViewScoped
public class SnsController implements Controller {

	private static final Logger LOGGER = LoggerFactory.getLogger(SnsController.class);

	private final SnsService snsService;
	private final NavigationManager navigationManager;

	private List<SnsTopic> topicos = new ArrayList<>();
	private List<SnsSubscription> assinaturas = new ArrayList<>();

	private String filtroTopico;
	private SnsTopic topicoSelecionado;
	private String subjectEnvio;
	private String bodyEnvio;
	private String messageAttributesEnvio;

	public SnsController(SnsService snsService, NavigationManager navigationManager) {
		this.snsService = snsService;
		this.navigationManager = navigationManager;
	}

	@PostConstruct
	public void init() {
		this.carregarTopicos();
		this.selecionarTopicoDaRequisicao();
	}

	private void selecionarTopicoDaRequisicao() {
		this.navigationManager.getRequestedResource(ApplicationRoute.SNS)
				.ifPresent(this::selecionarTopicoPorIdentificador);
	}

	public void carregarTopicos() {
		try {
			this.topicos = new ArrayList<>(this.snsService.listarTopicos());
		} catch (Exception e) {
			LOGGER.error("Erro carregando topicos SNS", e);
			this.topicos = new ArrayList<>();
			this.adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Erro", "Erro ao carregar topicos SNS.");
		}
	}

	public void selecionarTopico(SnsTopic topico) {
		try {
			this.topicoSelecionado = topico;
			this.carregarAssinaturas();
			this.adicionarMensagem(FacesMessage.SEVERITY_INFO, "Topico selecionado", topico.getNome());
		} catch (Exception e) {
			LOGGER.error("Erro selecionando topico {}", topico.getNome(), e);
			this.adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Erro", "Erro ao selecionar topico.");
		}
	}

	public void atualizarTudo() {
		this.carregarTopicos();

		if (this.topicoSelecionado != null) {
			this.selecionarTopicoPorIdentificador(this.topicoSelecionado.getArn());
		}
	}

	public void publicarMensagem() {
		if (this.topicoSelecionado == null) {
			this.adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Topico obrigatorio", "Selecione um topico SNS.");
			return;
		}
		if (!this.validarJson(this.bodyEnvio, "Message")) {
			return;
		}
		if (this.messageAttributesEnvio != null
				&& !this.messageAttributesEnvio.isBlank()
				&& !this.validarJson(this.messageAttributesEnvio, "Message Attributes")) {
			return;
		}

		try {
			this.snsService.publicarMensagem(
					this.topicoSelecionado.getArn(),
					this.subjectEnvio,
					this.bodyEnvio,
					this.messageAttributesEnvio);
			this.subjectEnvio = null;
			this.bodyEnvio = null;
			this.messageAttributesEnvio = null;
			this.carregarAssinaturas();
			this.adicionarMensagem(FacesMessage.SEVERITY_INFO, "Mensagem publicada", "Mensagem enviada para o topico SNS.");
		} catch (Exception e) {
			LOGGER.error("Erro publicando mensagem no topico {}", this.topicoSelecionado.getNome(), e);
			this.adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Erro", "Erro ao publicar mensagem.");
		}
	}

	private void selecionarTopicoPorIdentificador(String identificador) {
		this.topicos.stream()
				.filter(topico -> identificador.equals(topico.getArn()) || identificador.equals(topico.getNome()))
				.findFirst()
				.ifPresent(this::selecionarTopico);
	}

	private void carregarAssinaturas() {
		if (this.topicoSelecionado == null) {
			this.assinaturas = new ArrayList<>();
			return;
		}
		this.assinaturas = new ArrayList<>(this.snsService.listarAssinaturas(this.topicoSelecionado.getArn()));
	}

	private boolean validarJson(String conteudo, String campo) {
		if (conteudo == null || conteudo.isBlank()) {
			this.adicionarMensagem(FacesMessage.SEVERITY_ERROR, "JSON obrigatorio", "Informe o conteudo do campo %s.".formatted(campo));
			return false;
		}
		if (!this.snsService.isJsonValido(conteudo)) {
			this.adicionarMensagem(FacesMessage.SEVERITY_ERROR, "JSON invalido", "O campo %s nao contem um JSON valido.".formatted(campo));
			return false;
		}

		return true;
	}

	public List<SnsTopic> getTopicosFiltrados() {
		if (this.filtroTopico == null || this.filtroTopico.isBlank()) {
			return this.topicos;
		}

		var filtro = this.filtroTopico.toLowerCase();
		return this.topicos.stream()
				.filter(topico -> topico.getNome().toLowerCase().contains(filtro)
						|| topico.getArn().toLowerCase().contains(filtro))
				.toList();
	}

	public List<SnsSubscription> getAssinaturas() {
		return this.assinaturas;
	}

	public SnsTopic getTopicoSelecionado() {
		return this.topicoSelecionado;
	}

	public String getFiltroTopico() {
		return this.filtroTopico;
	}

	public void setFiltroTopico(String filtroTopico) {
		this.filtroTopico = filtroTopico;
	}

	public String getSubjectEnvio() {
		return this.subjectEnvio;
	}

	public void setSubjectEnvio(String subjectEnvio) {
		this.subjectEnvio = subjectEnvio;
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
}
