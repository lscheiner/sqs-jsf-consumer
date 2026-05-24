package br.com.scheiner.aws.console.config;

import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RedisConnectionConfiguration {

	private final AtomicReference<String> hostRef = new AtomicReference<>();

	private final AtomicReference<Integer> portRef = new AtomicReference<>();

	private final AtomicReference<Boolean> tlsRef = new AtomicReference<>();

	private final AtomicReference<String> usernameRef = new AtomicReference<>();

	private final AtomicReference<String> passwordRef = new AtomicReference<>();

	public RedisConnectionConfiguration(
			@Value("${redis.host}") String host,
			@Value("${redis.port}") Integer port,
			@Value("${redis.tls}") Boolean tls,
			@Value("${redis.username}") String username,
			@Value("${redis.password}") String password) {

		this.hostRef.set(host);
		this.portRef.set(port);
		this.tlsRef.set(tls);
		this.usernameRef.set(username);
		this.passwordRef.set(password);
	}

	public String getHost() {
		return this.hostRef.get();
	}

	public void setHost(String host) {
		this.hostRef.set(host);
	}

	public Integer getPort() {
		return this.portRef.get();
	}

	public void setPort(Integer port) {
		this.portRef.set(port);
	}

	public Boolean getTls() {
		return this.tlsRef.get();
	}

	public void setTls(Boolean tls) {
		this.tlsRef.set(tls);
	}

	public String getUsername() {
		return this.usernameRef.get();
	}

	public void setUsername(String username) {
		this.usernameRef.set(username);
	}

	public String getPassword() {
		return this.passwordRef.get();
	}

	public void setPassword(String password) {
		this.passwordRef.set(password);
	}
}
