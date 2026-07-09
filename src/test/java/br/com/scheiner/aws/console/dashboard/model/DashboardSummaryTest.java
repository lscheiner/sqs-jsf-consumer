package br.com.scheiner.aws.console.dashboard.model;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.scheiner.aws.console.resource.model.ServiceStatus;

class DashboardSummaryTest {

	@Test
	@DisplayName("Deve indicar conexao apenas quando os status estiverem conectados")
	void deve_indicar_conexao_apenas_quando_os_status_estiverem_conectados() {
		var summary = new DashboardSummary();

		assertThat(summary.isLocalstackConnected()).isFalse();
		assertThat(summary.isRedisConnected()).isFalse();

		summary.setLocalstackStatus(ServiceStatus.CONNECTED);
		summary.setRedisStatus(ServiceStatus.DISCONNECTED);

		assertThat(summary.isLocalstackConnected()).isTrue();
		assertThat(summary.isRedisConnected()).isFalse();
	}

	@Test
	@DisplayName("Deve armazenar os totais e enderecos exibidos no dashboard")
	void deve_armazenar_totais_e_enderecos_exibidos_no_dashboard() {
		var summary = new DashboardSummary();

		summary.setSqsQueueCount(1);
		summary.setDynamoTableCount(2);
		summary.setSnsTopicCount(3);
		summary.setRedisKeyCount(4);
		summary.setLocalstackEndpoint("http://localhost:4566");
		summary.setRedisHost("localhost:6379");

		assertThat(summary.getSqsQueueCount()).isEqualTo(1);
		assertThat(summary.getDynamoTableCount()).isEqualTo(2);
		assertThat(summary.getSnsTopicCount()).isEqualTo(3);
		assertThat(summary.getRedisKeyCount()).isEqualTo(4);
		assertThat(summary.getLocalstackEndpoint()).isEqualTo("http://localhost:4566");
		assertThat(summary.getRedisHost()).isEqualTo("localhost:6379");
	}
	
	@Test
	@DisplayName("Deve refletir o status de conexao para todos os estados dos servicos")
	void deve_refletir_o_status_de_conexao_para_todos_os_estados_dos_servicos() {
		var summary = new DashboardSummary();

		summary.setLocalstackStatus(ServiceStatus.DISCONNECTED);
		assertThat(summary.isLocalstackConnected()).isFalse();

		summary.setRedisStatus(ServiceStatus.CONNECTED);
		assertThat(summary.isRedisConnected()).isTrue();
	}
}
