package br.com.scheiner.aws.console.sqs.model;



import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;

class SqsModelTest {

	@Test
	@DisplayName("Deve expor dados imutaveis do resumo da fila")
	void deve_expor_dados_imutaveis_do_resumo_da_fila() {
		var fila = new SqsExplorerQueue("fila", "url", "Standard", 3, true);

		assertThat(fila.getNome()).isEqualTo("fila");
		assertThat(fila.getUrl()).isEqualTo("url");
		assertThat(fila.getTipo()).isEqualTo("Standard");
		assertThat(fila.getQuantidadeMensagens()).isEqualTo(3);
		assertThat(fila.isDlq()).isTrue();
	}

	@Test
	@DisplayName("Deve armazenar dados completos da mensagem SQS")
	void deve_armazenar_dados_completos_da_mensagem_sqs() {
		var atributo = MessageAttributeValue.builder().dataType("String").stringValue("teste").build();
		var mensagem = new SqsExplorerMessage();

		mensagem.setMessageId("id");
		mensagem.setReceiptHandle("receipt");
		mensagem.setBody("{}");
		mensagem.setBodyFormatado("{ }");
		mensagem.setSentTimestamp("100");
		mensagem.setReceiveCount("2");
		mensagem.setAttributes(Map.of("attr", "valor"));
		mensagem.setMessageAttributes(Map.of("origem", atributo));

		assertThat(mensagem.getMessageId()).isEqualTo("id");
		assertThat(mensagem.getReceiptHandle()).isEqualTo("receipt");
		assertThat(mensagem.getBody()).isEqualTo("{}");
		assertThat(mensagem.getBodyFormatado()).isEqualTo("{ }");
		assertThat(mensagem.getSentTimestamp()).isEqualTo("100");
		assertThat(mensagem.getReceiveCount()).isEqualTo("2");
		assertThat(mensagem.getAttributes()).containsEntry("attr", "valor");
		assertThat(mensagem.getMessageAttributes()).containsEntry("origem", atributo);
	}

	@Test
	@DisplayName("Deve indicar presenca de DLQ e fila original apenas quando campos estiverem preenchidos")
	void deve_indicar_presenca_de_dlq_e_fila_original_apenas_quando_campos_estiverem_preenchidos() {
		var detalhes = new SqsQueueDetails();

		assertThat(detalhes.isPossuiDlq()).isFalse();
		assertThat(detalhes.isDlqComFilaOriginal()).isFalse();

		detalhes.setDlqArn("arn:dlq");
		detalhes.setFilaOriginalUrl("url-original");

		assertThat(detalhes.isPossuiDlq()).isTrue();
		assertThat(detalhes.isDlqComFilaOriginal()).isTrue();
	}

	@Test
	@DisplayName("Deve armazenar detalhes da fila SQS")
	void deve_armazenar_detalhes_da_fila_sqs() {
		var detalhes = new SqsQueueDetails();

		detalhes.setNome("fila");
		detalhes.setUrl("url");
		detalhes.setArn("arn");
		detalhes.setTipo("Standard");
		detalhes.setVisibilityTimeout("30");
		detalhes.setMessageRetentionPeriod("86400");
		detalhes.setReceiveMessageWaitTime("0");
		detalhes.setDlqNome("dlq");
		detalhes.setMaxReceiveCount("5");
		detalhes.setFilaOriginalNome("original");

		assertThat(detalhes.getNome()).isEqualTo("fila");
		assertThat(detalhes.getUrl()).isEqualTo("url");
		assertThat(detalhes.getArn()).isEqualTo("arn");
		assertThat(detalhes.getTipo()).isEqualTo("Standard");
		assertThat(detalhes.getVisibilityTimeout()).isEqualTo("30");
		assertThat(detalhes.getMessageRetentionPeriod()).isEqualTo("86400");
		assertThat(detalhes.getReceiveMessageWaitTime()).isEqualTo("0");
		assertThat(detalhes.getDlqNome()).isEqualTo("dlq");
		assertThat(detalhes.getMaxReceiveCount()).isEqualTo("5");
		assertThat(detalhes.getFilaOriginalNome()).isEqualTo("original");
	}
}
