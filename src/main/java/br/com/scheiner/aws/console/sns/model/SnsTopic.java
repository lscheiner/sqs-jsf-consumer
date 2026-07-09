package br.com.scheiner.aws.console.sns.model;

public class SnsTopic {

	private String nome;
	private String arn;
	private int quantidadeAssinaturas;

	public SnsTopic() {
	}

	public SnsTopic(String nome, String arn, int quantidadeAssinaturas) {
		this.nome = nome;
		this.arn = arn;
		this.quantidadeAssinaturas = quantidadeAssinaturas;
	}

	public String getNome() {
		return this.nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getArn() {
		return this.arn;
	}

	public void setArn(String arn) {
		this.arn = arn;
	}

	public int getQuantidadeAssinaturas() {
		return this.quantidadeAssinaturas;
	}

	public void setQuantidadeAssinaturas(int quantidadeAssinaturas) {
		this.quantidadeAssinaturas = quantidadeAssinaturas;
	}
}
