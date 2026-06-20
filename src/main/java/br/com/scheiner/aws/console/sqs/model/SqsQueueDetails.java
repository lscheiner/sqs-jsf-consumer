package br.com.scheiner.aws.console.sqs.model;

public class SqsQueueDetails {

	private String nome;

	private String url;

	private String arn;

	private String tipo;

	private String visibilityTimeout;

	private String messageRetentionPeriod;

	private String receiveMessageWaitTime;

	private String dlqNome;

	private String dlqArn;

	private String maxReceiveCount;

	private String filaOriginalNome;

	private String filaOriginalUrl;

	public String getNome() {
		return this.nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getArn() {
		return this.arn;
	}

	public void setArn(String arn) {
		this.arn = arn;
	}

	public String getTipo() {
		return this.tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getVisibilityTimeout() {
		return this.visibilityTimeout;
	}

	public void setVisibilityTimeout(String visibilityTimeout) {
		this.visibilityTimeout = visibilityTimeout;
	}

	public String getMessageRetentionPeriod() {
		return this.messageRetentionPeriod;
	}

	public void setMessageRetentionPeriod(String messageRetentionPeriod) {
		this.messageRetentionPeriod = messageRetentionPeriod;
	}

	public String getReceiveMessageWaitTime() {
		return this.receiveMessageWaitTime;
	}

	public void setReceiveMessageWaitTime(String receiveMessageWaitTime) {
		this.receiveMessageWaitTime = receiveMessageWaitTime;
	}

	public String getDlqNome() {
		return this.dlqNome;
	}

	public void setDlqNome(String dlqNome) {
		this.dlqNome = dlqNome;
	}

	public String getDlqArn() {
		return this.dlqArn;
	}

	public void setDlqArn(String dlqArn) {
		this.dlqArn = dlqArn;
	}

	public String getMaxReceiveCount() {
		return this.maxReceiveCount;
	}

	public void setMaxReceiveCount(String maxReceiveCount) {
		this.maxReceiveCount = maxReceiveCount;
	}

	public String getFilaOriginalNome() {
		return this.filaOriginalNome;
	}

	public void setFilaOriginalNome(String filaOriginalNome) {
		this.filaOriginalNome = filaOriginalNome;
	}

	public String getFilaOriginalUrl() {
		return this.filaOriginalUrl;
	}

	public void setFilaOriginalUrl(String filaOriginalUrl) {
		this.filaOriginalUrl = filaOriginalUrl;
	}

	public boolean isPossuiDlq() {
		return this.dlqArn != null && !this.dlqArn.isBlank();
	}

	public boolean isDlqComFilaOriginal() {
		return this.filaOriginalUrl != null && !this.filaOriginalUrl.isBlank();
	}
}
