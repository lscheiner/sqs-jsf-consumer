package br.com.scheiner.aws.console.sqs.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import br.com.scheiner.aws.console.sqs.gateway.SqsClientGateway;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageResponse;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesResponse;
import software.amazon.awssdk.services.sqs.model.ListQueuesRequest;
import software.amazon.awssdk.services.sqs.model.ListQueuesResponse;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.MessageSystemAttributeName;
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest;
import software.amazon.awssdk.services.sqs.model.PurgeQueueResponse;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

class SqsExplorerServiceTest {

	private static final String BASE = "http://localhost:4566/000000000000/";
	private static final String FILA = BASE + "pedido-criado";
	private static final String DLQ = BASE + "pedido-criado-dlq";
	private static final String FILA_FIFO = BASE + "pedido.fifo";
	private static final String ARN_FILA = "arn:aws:sqs:sa-east-1:000000000000:pedido-criado";
	private static final String ARN_DLQ = "arn:aws:sqs:sa-east-1:000000000000:pedido-criado-dlq";

	private final SqsClient client = mock(SqsClient.class);
	private final SqsClientGateway gateway = new GatewayFake(this.client);
	private final SqsExplorerService service = new SqsExplorerService(this.gateway);

	@Test
	@DisplayName("Deve listar filas ordenadas identificando quantidade, tipo e DLQ")
	void deve_listar_filas_ordenadas_identificando_quantidade_tipo_e_dlq() {
		when(this.client.listQueues(any(ListQueuesRequest.class)))
				.thenReturn(ListQueuesResponse.builder().queueUrls(FILA, DLQ, FILA_FIFO).build());
		stubAtributosPadrao();

		var filas = this.service.listarFilas();

		assertThat(filas).extracting("nome")
				.containsExactly("pedido-criado", "pedido-criado-dlq", "pedido.fifo");
		assertThat(filas.get(0).getQuantidadeMensagens()).isEqualTo(4);
		assertThat(filas.get(0).getTipo()).isEqualTo("Standard");
		assertThat(filas.get(0).isDlq()).isFalse();
		assertThat(filas.get(1).isDlq()).isTrue();
		assertThat(filas.get(2).getTipo()).isEqualTo("FIFO");
	}

	@Test
	@DisplayName("Deve buscar detalhes da DLQ com referencia para a fila original")
	void deve_buscar_detalhes_da_dlq_com_referencia_para_a_fila_original() {
		when(this.client.listQueues()).thenReturn(ListQueuesResponse.builder().queueUrls(FILA, DLQ).build());
		stubAtributosPadrao();

		var detalhes = this.service.buscarDetalhes(DLQ);

		assertThat(detalhes.getNome()).isEqualTo("pedido-criado-dlq");
		assertThat(detalhes.getUrl()).isEqualTo(DLQ);
		assertThat(detalhes.getArn()).isEqualTo(ARN_DLQ);
		assertThat(detalhes.getTipo()).isEqualTo("Standard");
		assertThat(detalhes.getFilaOriginalUrl()).isEqualTo(FILA);
		assertThat(detalhes.getFilaOriginalNome()).isEqualTo("pedido-criado");
		assertThat(detalhes.isDlqComFilaOriginal()).isTrue();
	}

	@Test
	@DisplayName("Deve buscar detalhes da fila com politica de redrive")
	void deve_buscar_detalhes_da_fila_com_politica_de_redrive() {
		when(this.client.listQueues()).thenReturn(ListQueuesResponse.builder().queueUrls(FILA, DLQ).build());
		stubAtributosPadrao();

		var detalhes = this.service.buscarDetalhes(FILA);

		assertThat(detalhes.getNome()).isEqualTo("pedido-criado");
		assertThat(detalhes.getDlqArn()).isEqualTo(ARN_DLQ);
		assertThat(detalhes.getDlqNome()).isEqualTo("pedido-criado-dlq");
		assertThat(detalhes.getMaxReceiveCount()).isEqualTo("5");
		assertThat(detalhes.isPossuiDlq()).isTrue();
		assertThat(detalhes.getVisibilityTimeout()).isEqualTo("30");
		assertThat(detalhes.getMessageRetentionPeriod()).isEqualTo("86400");
	}

	@Test
	@DisplayName("Deve converter mensagens recebidas preservando atributos e corpo formatado")
	void deve_converter_mensagens_recebidas_preservando_atributos_e_corpo_formatado() {
		var atributoMensagem = MessageAttributeValue.builder().dataType("String").stringValue("teste").build();
		var mensagem = Message.builder()
				.messageId("msg-1")
				.receiptHandle("receipt")
				.body("{\"ok\":true}")
				.attributes(Map.of(
						MessageSystemAttributeName.SENT_TIMESTAMP, "1000",
						MessageSystemAttributeName.APPROXIMATE_RECEIVE_COUNT, "2"))
				.messageAttributes(Map.of("origem", atributoMensagem))
				.build();
		when(this.client.receiveMessage(any(ReceiveMessageRequest.class)))
				.thenReturn(ReceiveMessageResponse.builder().messages(mensagem).build());

		var mensagens = this.service.buscarMensagens(FILA, 3);
		var resultado = mensagens.getFirst();

		assertThat(mensagens).hasSize(1);
		assertThat(resultado.getMessageId()).isEqualTo("msg-1");
		assertThat(resultado.getReceiptHandle()).isEqualTo("receipt");
		assertThat(resultado.getBody()).isEqualTo("{\"ok\":true}");
		assertThat(resultado.getBodyFormatado()).contains("\"ok\" : true");
		assertThat(resultado.getSentTimestamp()).isEqualTo("1000");
		assertThat(resultado.getReceiveCount()).isEqualTo("2");
		assertThat(resultado.getMessageAttributes()).containsEntry("origem", atributoMensagem);
	}

	@Test
	@DisplayName("Deve enviar mensagem com atributo padrao de JSON quando atributos nao forem informados")
	void deve_enviar_mensagem_com_atributo_padrao_de_json_quando_atributos_nao_foremm_informados() {
		var captor = ArgumentCaptor.forClass(SendMessageRequest.class);
		when(this.client.sendMessage(captor.capture())).thenReturn(SendMessageResponse.builder().messageId("1").build());

		this.service.enviarMensagem(FILA, "{\"ok\":true}", "");

		var request = captor.getValue();
		assertThat(request.queueUrl()).isEqualTo(FILA);
		assertThat(request.messageBody()).isEqualTo("{\"ok\":true}");
		assertThat(request.messageAttributes().get("content-type").stringValue()).isEqualTo("application/json");
	}

	@Test
	@DisplayName("Deve enviar mensagem com atributos informados em JSON")
	void deve_enviar_mensagem_com_atributos_informados_em_json() {
		var captor = ArgumentCaptor.forClass(SendMessageRequest.class);
		when(this.client.sendMessage(captor.capture())).thenReturn(SendMessageResponse.builder().messageId("1").build());

		this.service.enviarMensagem(FILA, "{}", "{\"origem\":{\"dataType\":\"String\",\"stringValue\":\"teste\"}}");

		assertThat(captor.getValue().messageAttributes().get("origem").stringValue()).isEqualTo("teste");
	}

	@Test
	@DisplayName("Deve rejeitar atributos de mensagem invalidos")
	void deve_rejeitar_atributos_de_mensagem_invalidos() {
		var excecao = assertThrows(
				IllegalArgumentException.class,
				() -> this.service.enviarMensagem(FILA, "{}", "{invalido"));

		assertThat(excecao.getMessage()).contains("Message Attributes");
	}

	@Test
	@DisplayName("Deve excluir mensagem usando receipt handle")
	void deve_excluir_mensagem_usando_receipt_handle() {
		var captor = ArgumentCaptor.forClass(DeleteMessageRequest.class);
		when(this.client.deleteMessage(captor.capture())).thenReturn(DeleteMessageResponse.builder().build());

		this.service.excluirMensagem(FILA, "receipt");

		assertThat(captor.getValue().queueUrl()).isEqualTo(FILA);
		assertThat(captor.getValue().receiptHandle()).isEqualTo("receipt");
	}

	@Test
	@DisplayName("Deve executar purge na fila selecionada")
	void deve_executar_purge_na_fila_selecionada() {
		var captor = ArgumentCaptor.forClass(PurgeQueueRequest.class);
		when(this.client.purgeQueue(captor.capture())).thenReturn(PurgeQueueResponse.builder().build());

		this.service.purge(FILA);

		assertThat(captor.getValue().queueUrl()).isEqualTo(FILA);
	}

	@Test
	@DisplayName("Deve montar URL da fila pelo gateway")
	void deve_montar_url_da_fila_pelo_gateway() {
		assertThat(this.service.buscarQueueUrlPorNome("fila")).isEqualTo(BASE + "fila");
	}

	@Test
	@DisplayName("Deve validar JSON informado")
	void deve_validar_json_informado() {
		assertThat(this.service.isJsonValido("{\"ok\":true}")).isTrue();
		assertThat(this.service.isJsonValido("{invalido")).isFalse();
	}

	private void stubAtributosPadrao() {
		when(this.client.getQueueAttributes(any(GetQueueAttributesRequest.class)))
				.thenAnswer(invocation -> {
					GetQueueAttributesRequest request = invocation.getArgument(0);
					return GetQueueAttributesResponse.builder()
							.attributes(atributos(request.queueUrl()))
							.build();
				});
	}

	private static Map<QueueAttributeName, String> atributos(String url) {
		if (FILA.equals(url)) {
			return Map.of(
					QueueAttributeName.QUEUE_ARN, ARN_FILA,
					QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES, "4",
					QueueAttributeName.VISIBILITY_TIMEOUT, "30",
					QueueAttributeName.MESSAGE_RETENTION_PERIOD, "86400",
					QueueAttributeName.RECEIVE_MESSAGE_WAIT_TIME_SECONDS, "0",
					QueueAttributeName.REDRIVE_POLICY,
					"{\"deadLetterTargetArn\":\"%s\",\"maxReceiveCount\":\"5\"}".formatted(ARN_DLQ));
		}
		if (DLQ.equals(url)) {
			return Map.of(
					QueueAttributeName.QUEUE_ARN, ARN_DLQ,
					QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES, "0",
					QueueAttributeName.VISIBILITY_TIMEOUT, "30",
					QueueAttributeName.MESSAGE_RETENTION_PERIOD, "86400",
					QueueAttributeName.RECEIVE_MESSAGE_WAIT_TIME_SECONDS, "0");
		}
		return Map.of(
				QueueAttributeName.QUEUE_ARN, "arn:aws:sqs:sa-east-1:000000000000:pedido.fifo",
				QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES, "nao-numerico",
				QueueAttributeName.FIFO_QUEUE, "true");
	}

	private record GatewayFake(SqsClient client) implements SqsClientGateway {
		@Override
		public SqsClient getClient() {
			return this.client;
		}

		@Override
		public String montarQueueUrl(String fila) {
			return BASE + fila;
		}

		@Override
		public String extrairNomeFila(String queueUrl) {
			return queueUrl.substring(queueUrl.lastIndexOf("/") + 1);
		}
	}
}
