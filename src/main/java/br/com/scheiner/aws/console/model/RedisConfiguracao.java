package br.com.scheiner.aws.console.model;

public class RedisConfiguracao {

	private String host;

	private Integer port;

	private Boolean tls;

	private String username;

	private String password;

	public RedisConfiguracao() {
	}

	public RedisConfiguracao(String host, Integer port, Boolean tls, String username, String password) {
		this.host = host;
		this.port = port;
		this.tls = tls;
		this.username = username;
		this.password = password;
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
}
