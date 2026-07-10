package br.com.scheiner.aws.console.sns.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.function.Consumer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import br.com.scheiner.aws.console.sns.gateway.SnsClientGateway;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.ListSubscriptionsByTopicRequest;
import software.amazon.awssdk.services.sns.model.ListSubscriptionsByTopicResponse;
import software.amazon.awssdk.services.sns.model.ListTopicsRequest;
import software.amazon.awssdk.services.sns.model.ListTopicsResponse;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.Subscription;
import software.amazon.awssdk.services.sns.model.Topic;
import software.amazon.awssdk.services.sns.paginators.ListSubscriptionsByTopicIterable;
import software.amazon.awssdk.services.sns.paginators.ListTopicsIterable;

class SnsServiceTest {

	private final SnsClient client = mock(SnsClient.class);
	private final SnsClientGateway gateway = () -> this.client;
	private final SnsService service = new SnsService(this.gateway);

	@Test
	@DisplayName("Deve listar topicos ordenados com quantidade de assinaturas")
	void deve_listar_topicos_ordenados_com_quantidade_de_assinaturas() {
		
		when(this.client.listTopicsPaginator(any(ListTopicsRequest.class)))
				.thenReturn(new ListTopicsIterable(this.client, ListTopicsRequest.builder().build()));
		when(this.client.listTopics(any(ListTopicsRequest.class)))
				.thenReturn(ListTopicsResponse.builder()
						.topics(Topic.builder().topicArn("arn:aws:sns:sa-east-1:000000000000:z-topico").build(),
								Topic.builder().topicArn("arn:aws:sns:sa-east-1:000000000000:a-topico").build())
						.build());
		when(this.client.listSubscriptionsByTopicPaginator(anyConsumer())).thenReturn(
				new ListSubscriptionsByTopicIterable(this.client, ListSubscriptionsByTopicRequest.builder().build()));
		when(this.client.listSubscriptionsByTopic(any(ListSubscriptionsByTopicRequest.class))).thenReturn(
				ListSubscriptionsByTopicResponse.builder().subscriptions(subscription("sqs", "fila")).build());

		var topicos = this.service.listarTopicos();

		assertThat(topicos).extracting("nome").containsExactly("a-topico", "z-topico");
		assertThat(topicos).extracting("quantidadeAssinaturas").containsExactly(1, 1);
	}

	@Test
	@DisplayName("Deve listar assinaturas de um topico")
	void deve_listar_assinaturas_de_um_topico() {
		when(this.client.listSubscriptionsByTopicPaginator(anyConsumer())).thenReturn(
				new ListSubscriptionsByTopicIterable(this.client, ListSubscriptionsByTopicRequest.builder().build()));
		when(this.client.listSubscriptionsByTopic(any(ListSubscriptionsByTopicRequest.class))).thenReturn(
				ListSubscriptionsByTopicResponse.builder().subscriptions(subscription("sqs", "arn:fila")).build());

		var assinaturas = this.service.listarAssinaturas("arn:topico");

		var assinatura = assinaturas.getFirst();
		assertThat(assinaturas).hasSize(1);
		assertThat(assinatura.getArn()).isEqualTo("arn:subscription");
		assertThat(assinatura.getProtocolo()).isEqualTo("sqs");
		assertThat(assinatura.getEndpoint()).isEqualTo("arn:fila");
	}

	@Test
	@DisplayName("Deve publicar mensagem usando atributos padrao quando nenhum atributo for informado")
	void deve_publicar_mensagem_usando_atributos_padrao_quando_nenhum_atributo_for_informado() {
		var captor = ArgumentCaptor.forClass(PublishRequest.class);
		when(this.client.publish(captor.capture())).thenReturn(PublishResponse.builder().messageId("1").build());

		this.service.publicarMensagem("arn:topico", "", "{\"ok\":true}", "");

		var request = captor.getValue();
		assertThat(request.topicArn()).isEqualTo("arn:topico");
		assertThat(request.subject()).isNull();
		assertThat(request.message()).isEqualTo("{\"ok\":true}");
		var contentType = request.messageAttributes().get("content-type");
		assertThat(request.messageAttributes()).containsKey("content-type");
		assertThat(contentType.stringValue()).isEqualTo("application/json");
	}

	@Test
	@DisplayName("Deve publicar mensagem usando atributos informados em JSON")
	void deve_publicar_mensagem_usando_atributos_informados_em_json() {
		var captor = ArgumentCaptor.forClass(PublishRequest.class);
		when(this.client.publish(captor.capture())).thenReturn(PublishResponse.builder().messageId("1").build());

		this.service.publicarMensagem("arn:topico", "assunto", "{\"ok\":true}",
				"{\"origem\":{\"dataType\":\"String\",\"stringValue\":\"teste\"}}");

		var request = captor.getValue();
		assertThat(request.subject()).isEqualTo("assunto");
		assertThat(request.messageAttributes().get("origem").dataType()).isEqualTo("String");
		assertThat(request.messageAttributes().get("origem").stringValue()).isEqualTo("teste");
	}

	@Test
	@DisplayName("Deve rejeitar atributos de mensagem com JSON invalido")
	void deve_rejeitar_atributos_de_mensagem_com_json_invalido() {
		var excecao = assertThrows(IllegalArgumentException.class,
				() -> this.service.publicarMensagem("arn", null, "{}", "{invalido"));

		assertThat(excecao.getMessage()).contains("Message Attributes");
	}

	@Test
	@DisplayName("Deve validar conteudo JSON")
	void deve_validar_conteudo_json() {
		assertThat(this.service.isJsonValido("{\"ok\":true}")).isTrue();
		assertThat(this.service.isJsonValido("{invalido")).isFalse();
	}

	@Test
	@DisplayName("Deve extrair nome do topico a partir do ARN")
	void deve_extrair_nome_do_topico_a_partir_do_arn() {
		assertThat(this.service.extrairNomeTopico("arn:aws:sns:sa-east-1:000000000000:pedido")).isEqualTo("pedido");
		assertThat(this.service.extrairNomeTopico(null)).isEmpty();
		assertThat(this.service.extrairNomeTopico("")).isEmpty();
	}

	@SuppressWarnings("unchecked")
	@Test
	@DisplayName("Deve passar o ARN correto do topico para o paginador de assinaturas")
	void deve_passar_o_arn_correto_do_topico_para_o_paginador_de_assinaturas() {

		var paginadorMock = mock(ListSubscriptionsByTopicIterable.class);

		var sdkIterableMock = mock(SdkIterable.class);

		when(paginadorMock.subscriptions()).thenReturn(sdkIterableMock);
		when(sdkIterableMock.stream()).thenReturn(java.util.stream.Stream.empty());

		ArgumentCaptor<Consumer<ListSubscriptionsByTopicRequest.Builder>> consumerCaptor = ArgumentCaptor
				.forClass(Consumer.class);

		when(this.client.listSubscriptionsByTopicPaginator(consumerCaptor.capture())).thenReturn(paginadorMock);

		this.service.listarAssinaturas("arn:aws:sns:sa-east-1:000000000000:meu-topico");

		var consumerExecutado = consumerCaptor.getValue();
		var builderReal = ListSubscriptionsByTopicRequest.builder();
		consumerExecutado.accept(builderReal);

		var requestConstruida = builderReal.build();
		assertThat(requestConstruida.topicArn()).isEqualTo("arn:aws:sns:sa-east-1:000000000000:meu-topico");
	}

	private static Subscription subscription(String protocolo, String endpoint) {
		return Subscription.builder().subscriptionArn("arn:subscription").protocol(protocolo).endpoint(endpoint)
				.build();
	}

	@SuppressWarnings("unchecked")
	private static Consumer<ListSubscriptionsByTopicRequest.Builder> anyConsumer() {
		return any(Consumer.class);
	}
}
