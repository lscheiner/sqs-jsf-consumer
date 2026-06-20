package br.com.scheiner.aws.console.resource.model;

public enum ServiceStatus {

	CONNECTED,
	DISCONNECTED,
	UNAVAILABLE;

	public boolean isConnected() {
		return this == CONNECTED;
	}
}
