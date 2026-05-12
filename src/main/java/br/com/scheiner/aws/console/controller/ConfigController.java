package br.com.scheiner.aws.console.controller;

import java.util.List;
import java.util.Optional;

import br.com.scheiner.aws.console.config.AwsConfiguration;
import br.com.scheiner.aws.console.config.AwsProvider;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import software.amazon.awssdk.regions.Region;

@Named
@ViewScoped
public class ConfigController implements SqsController {
	
	private String endpoint;

	private String region;

	private boolean connected;
	
	private final List<AwsProvider> awsProvider;
	
	private final AwsConfiguration awsConfiguration;

	public ConfigController(List<AwsProvider> awsProvider , AwsConfiguration awsConfiguration) {
		super();
		this.awsProvider = awsProvider;
		this.awsConfiguration = awsConfiguration;
		this.endpoint = awsConfiguration.getEndpoint();
		this.region = awsConfiguration.getRegion().id();
		this.testarConexao();
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

	public List<AwsProvider> getAwsProvider() {
		return awsProvider;
	}
	
	public List<String> getRegions() {
		return Region.regions().stream().map(Region::id).sorted().toList();
	}
	
	public void testarConexao() {
		
		this.connected = this.awsProvider.stream().anyMatch(AwsProvider::isConectado);

	    Optional.of(this.connected)
	            .filter(Boolean::booleanValue)
	            .ifPresentOrElse(
	                    conectado -> adicionarMensagem(FacesMessage.SEVERITY_INFO,"Conexão OK","Conexão realizada com sucesso."),

	                    () -> 
	                    adicionarMensagem(FacesMessage.SEVERITY_ERROR,"Erro de conexão","Não foi possível realizar a conexão.")
	            );
	}
	
	public void aplicarConfiguracao() {
		
		this.awsConfiguration.setEndpoint(this.endpoint);
		this.awsConfiguration.setRegion(Region.of(this.region));
		this.awsProvider.stream().forEach(f -> f.reconfigurar());
		this.testarConexao();
	}

}