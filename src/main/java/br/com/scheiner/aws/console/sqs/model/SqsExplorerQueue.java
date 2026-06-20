package br.com.scheiner.aws.console.sqs.model;

public class SqsExplorerQueue {

	private String nome;

	private String url;

	private String tipo;

	private Integer quantidadeMensagens;

	private boolean dlq;

	public SqsExplorerQueue(String nome, String url, String tipo, Integer quantidadeMensagens, boolean dlq) {
		this.nome = nome;
		this.url = url;
		this.tipo = tipo;
		this.quantidadeMensagens = quantidadeMensagens;
		this.dlq = dlq;
	}

	public String getNome() {
		return this.nome;
	}

	public String getUrl() {
		return this.url;
	}

	public String getTipo() {
		return this.tipo;
	}

	public Integer getQuantidadeMensagens() {
		return this.quantidadeMensagens;
	}

	public boolean isDlq() {
		return this.dlq;
	}
}
