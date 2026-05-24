package br.com.scheiner.aws.console.model;

public class RedisRegistro {

	private String chave;

	private String valor;

	private Long ttl;

	public RedisRegistro() {
	}

	public RedisRegistro(String chave, String valor, Long ttl) {
		this.chave = chave;
		this.valor = valor;
		this.ttl = ttl;
	}

	public String getChave() {
		return this.chave;
	}

	public void setChave(String chave) {
		this.chave = chave;
	}

	public String getValor() {
		return this.valor;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}

	public Long getTtl() {
		return this.ttl;
	}

	public void setTtl(Long ttl) {
		this.ttl = ttl;
	}
}
