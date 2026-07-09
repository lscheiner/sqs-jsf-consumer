package br.com.scheiner.aws.console.sns.model;

public class SnsSubscription {

	private String arn;
	private String protocolo;
	private String endpoint;

	public SnsSubscription() {
	}

	public SnsSubscription(String arn, String protocolo, String endpoint) {
		this.arn = arn;
		this.protocolo = protocolo;
		this.endpoint = endpoint;
	}

	public String getArn() {
		return this.arn;
	}

	public void setArn(String arn) {
		this.arn = arn;
	}

	public String getProtocolo() {
		return this.protocolo;
	}

	public void setProtocolo(String protocolo) {
		this.protocolo = protocolo;
	}

	public String getEndpoint() {
		return this.endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}
}
