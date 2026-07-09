package br.com.scheiner.aws.console.configuration.aws;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import software.amazon.awssdk.regions.Region;

class AwsConfigurationTest {

	@Test
	@DisplayName("Deve manter endpoint e regiao atuais permitindo alteracao em runtime")
	void deve_manter_endpoint_e_regiao_atuais_permitindo_alteracao_em_runtime() {
		var configuracao = new AwsConfiguration("http://localhost:4566", "sa-east-1");

		configuracao.setEndpoint("http://localhost:9999");
		configuracao.setRegion(Region.US_EAST_1);

		assertThat(configuracao.getEndpoint()).isEqualTo("http://localhost:9999");
		assertThat(configuracao.getRegion()).isEqualTo(Region.US_EAST_1);

	}
}
