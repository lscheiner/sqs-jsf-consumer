package br.com.scheiner.aws.console.redis.model;

public class RedisRegistro {

	private String chave;

	private String valor;

	private String tipo;

	private Long ttl;

	public RedisRegistro() {
	}

	public RedisRegistro(String chave, String valor, Long ttl) {
		this(chave, valor, "string", ttl);
	}

	public RedisRegistro(String chave, String valor, String tipo, Long ttl) {
		this.chave = chave;
		this.valor = valor;
		this.tipo = tipo;
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

	public String getTipo() {
		return this.tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public boolean isEditavel() {
		return "string".equals(this.tipo);
	}

	public Long getTtl() {
		return this.ttl;
	}

	public void setTtl(Long ttl) {
		this.ttl = ttl;
	}
}
