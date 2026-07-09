package br.com.scheiner.aws.console.resource.model;



import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ServiceStatusTest {

	@Test
	@DisplayName("Deve considerar conectado apenas o status CONNECTED")
	void deve_considerar_conectado_apenas_o_status_connected() {
		assertThat(ServiceStatus.CONNECTED.isConnected()).isTrue();
		assertThat(ServiceStatus.DISCONNECTED.isConnected()).isFalse();
		assertThat(ServiceStatus.UNAVAILABLE.isConnected()).isFalse();
	}
}
