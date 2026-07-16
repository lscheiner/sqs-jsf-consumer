package br.com.scheiner.aws.console.redis.controller;

import java.util.ArrayList;
import java.util.List;

import org.primefaces.PrimeFaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.scheiner.aws.console.controller.Controller;
import br.com.scheiner.aws.console.redis.model.RedisConfiguracao;
import br.com.scheiner.aws.console.redis.model.RedisRegistro;
import br.com.scheiner.aws.console.redis.service.RedisService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

@Named
@ViewScoped
public class RedisController implements Controller {

	private static final Logger LOGGER = LoggerFactory.getLogger(RedisController.class);

	private static final int TAMANHO_PREVIEW_VALOR = 120;

	private final RedisService redisService;

	private List<RedisRegistro> registros = new ArrayList<>();

	private String chaveFormulario;

	private String valorFormulario;

	private Long ttlFormulario;

	private String chaveOriginal;

	private boolean novoRegistro;

	private RedisRegistro registroVisualizacao;

	private String host;

	private Integer port;

	private Boolean tls;

	private String username;

	private String password;

	private boolean connected;

	public RedisController(RedisService redisService) {
		this.redisService = redisService;
	}

	@PostConstruct
	public void init() {
		this.carregarConfiguracao();
		this.testarConexao();
		this.atualizarRegistros();
	}

	public void atualizarRegistros() {
		this.registros = new ArrayList<>(this.redisService.listarRegistros());
	}
	
	public void atualizarRegistrosGrid() {
		try {
			this.atualizarRegistros();
			this.adicionarMensagem(FacesMessage.SEVERITY_INFO, "Registros atualizados", "Registros Redis recarregados com sucesso.");
		} catch (Exception e) {
			LOGGER.error("Erro carregando registros Redis", e);
			this.registros = new ArrayList<>();
			this.adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Erro", "Erro ao carregar registros Redis.");
		}
	}

	public void novoRegistro() {
		this.chaveFormulario = null;
		this.valorFormulario = null;
		this.ttlFormulario = null;
		this.chaveOriginal = null;
		this.novoRegistro = true;
	}

	public void editarRegistro(RedisRegistro registro) {
		this.chaveFormulario = registro.getChave();
		this.valorFormulario = registro.getValor();
		this.ttlFormulario = this.ttlEditavel(registro.getTtl());
		this.chaveOriginal = registro.getChave();
		this.novoRegistro = false;
	}

	public void visualizarRegistro(RedisRegistro registro) {
		this.registroVisualizacao = registro;
	}

	public void salvarRegistro() {
		try {
			this.redisService.salvarRegistro(this.chaveFormulario, this.valorFormulario, this.ttlFormulario);

			if (!this.novoRegistro && !this.chaveFormulario.equals(this.chaveOriginal)) {
				this.redisService.excluirRegistro(this.chaveOriginal);
			}

			this.atualizarRegistros();
			PrimeFaces.current().ajax().addCallbackParam("salvo", true);
			this.adicionarMensagem(
					FacesMessage.SEVERITY_INFO,
					this.novoRegistro ? "Registro criado" : "Registro salvo",
					this.novoRegistro ? "Registro Redis criado com sucesso." : "Registro Redis atualizado com sucesso.");
		} catch (Exception e) {
			LOGGER.error("Erro salvando registro Redis", e);
			PrimeFaces.current().ajax().addCallbackParam("salvo", false);
			this.adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Erro", "Erro ao salvar registro Redis.");
		}
	}

	public void excluirRegistro(RedisRegistro registro) {
		try {
			this.redisService.excluirRegistro(registro.getChave());
			this.atualizarRegistros();
			this.adicionarMensagem(FacesMessage.SEVERITY_INFO, "Registro excluído", "Registro Redis removido com sucesso.");
		} catch (Exception e) {
			LOGGER.error("Erro excluindo registro Redis", e);
			this.adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Erro", "Erro ao excluir registro Redis.");
		}
	}

	public void testarConexao() {
		this.connected = this.redisService.testarConexao();

		if (this.connected) {
			this.adicionarMensagem(FacesMessage.SEVERITY_INFO, "Conexão Redis OK", "Conexão realizada com sucesso.");
			return;
		}

		this.adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Erro de conexão Redis", "Não foi possível conectar ao Redis.");
	}

	public void aplicarConfiguracao() {
		try {
			this.redisService.aplicarConfiguracao(new RedisConfiguracao(
					this.host,
					this.port,
					this.tls,
					this.username,
					this.password));
			this.testarConexao();
			this.atualizarRegistros();
		} catch (Exception e) {
			LOGGER.error("Erro aplicando configuração Redis", e);
			this.carregarConfiguracao();
			this.adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Configuração inválida", "Não foi possível aplicar a configuração Redis.");
		}
	}

	public String formatarTtl(Long ttl) {

        if (ttl == null) {
            return "-";
        }

        return switch (ttl.intValue()) {
            case -1 -> "Sem expiração";
            case -2 -> "Expirado";
            default -> "%d segundos".formatted(ttl);
        };
    }

	public String previewValor(String valor) {
		if (valor == null || valor.isBlank()) {
			return "";
		}

		var valorNormalizado = valor.replaceAll("\\s+", " ").trim();

		if (valorNormalizado.length() <= TAMANHO_PREVIEW_VALOR) {
			return valorNormalizado;
		}

		return valorNormalizado.substring(0, TAMANHO_PREVIEW_VALOR).concat("...");
	}

	private void carregarConfiguracao() {
		var configuracao = this.redisService.carregarConfiguracao();
		this.host = configuracao.getHost();
		this.port = configuracao.getPort();
		this.tls = configuracao.getTls();
		this.username = configuracao.getUsername();
		this.password = configuracao.getPassword();
	}

	private Long ttlEditavel(Long ttl) {
		return ttl != null && ttl > 0 ? ttl : null;
	}

	public List<RedisRegistro> getRegistros() {
		return this.registros;
	}

	public String getChaveFormulario() {
		return this.chaveFormulario;
	}

	public void setChaveFormulario(String chaveFormulario) {
		this.chaveFormulario = chaveFormulario;
	}

	public String getValorFormulario() {
		return this.valorFormulario;
	}

	public void setValorFormulario(String valorFormulario) {
		this.valorFormulario = valorFormulario;
	}

	public Long getTtlFormulario() {
		return this.ttlFormulario;
	}

	public void setTtlFormulario(Long ttlFormulario) {
		this.ttlFormulario = ttlFormulario;
	}

	public boolean isNovoRegistro() {
		return this.novoRegistro;
	}

	public RedisRegistro getRegistroVisualizacao() {
		return this.registroVisualizacao;
	}

	public String getHost() {
		return this.host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getPort() {
		return this.port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public Boolean getTls() {
		return this.tls;
	}

	public void setTls(Boolean tls) {
		this.tls = tls;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isConnected() {
		return this.connected;
	}
}
