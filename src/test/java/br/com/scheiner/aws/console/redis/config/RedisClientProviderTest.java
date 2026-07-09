package br.com.scheiner.aws.console.redis.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

class RedisClientProviderTest {

	private RedisConnectionConfiguration redisConnectionConfiguration;
	private RedisClientProvider redisClientProvider;
	private RedisClient client;
	private StatefulRedisConnection<String, String> connection;
	private RedisCommands<String, String> commands;

	@SuppressWarnings("unchecked")
	private <T> T criarMock(Class<?> tipo) {
		return (T) mock(tipo);
	}
	
	@BeforeEach
	void setup() {
		redisConnectionConfiguration = mock(RedisConnectionConfiguration.class);

		when(redisConnectionConfiguration.getHost()).thenReturn("localhost");
		when(redisConnectionConfiguration.getPort()).thenReturn(6379);
		when(redisConnectionConfiguration.getTls()).thenReturn(false);
		when(redisConnectionConfiguration.getUsername()).thenReturn(null);
		when(redisConnectionConfiguration.getPassword()).thenReturn(null);

		client = mock(RedisClient.class);
		connection = criarMock(StatefulRedisConnection.class);
		commands = criarMock(RedisCommands.class);

		try (var redisClientMockedStatic = mockStatic(RedisClient.class)) {
			redisClientMockedStatic.when(() -> RedisClient.create(any(RedisURI.class))).thenReturn(client);
			redisClientProvider = new RedisClientProvider(redisConnectionConfiguration);
		}
		
		ReflectionTestUtils.setField(redisClientProvider, "client", client);
	}

	@Test
	@DisplayName("Deve reutilizar conexão aberta")
	void deve_reutilizar_conexao_aberta() {
		when(connection.isOpen()).thenReturn(true);
		when(connection.sync()).thenReturn(commands);

		ReflectionTestUtils.setField(redisClientProvider, "connection", connection);

		var resultado = redisClientProvider.getCommands();

		assertThat(resultado).isSameAs(commands);
		verify(client, never()).connect();
		verify(connection).sync();
	}

	@Test
	@DisplayName("Deve criar conexão quando ela for nula")
	void deve_criar_conexao_quando_ela_for_nula() {
		when(client.connect()).thenReturn(connection);
		when(connection.sync()).thenReturn(commands);

		var resultado = redisClientProvider.getCommands();

		assertThat(resultado).isSameAs(commands);
		verify(client).connect();
		verify(connection).sync();
	}

	@Test
	@DisplayName("Deve recriar conexão quando ela estiver fechada")
	void deve_recriar_conexao_quando_ela_estiver_fechada() {
		when(connection.isOpen()).thenReturn(false);
		when(client.connect()).thenReturn(connection);
		when(connection.sync()).thenReturn(commands);

		ReflectionTestUtils.setField(redisClientProvider, "connection", connection);

		var resultado = redisClientProvider.getCommands();

		assertThat(resultado).isSameAs(commands);
		verify(client).connect();
		verify(connection).sync();
	}

	@Test
	@DisplayName("Deve retornar verdadeiro quando ping for executado com sucesso")
	void deve_retornar_verdadeiro_quando_ping_for_executado_com_sucesso() {
		when(connection.isOpen()).thenReturn(true);
		when(connection.sync()).thenReturn(commands);

		ReflectionTestUtils.setField(redisClientProvider, "connection", connection);

		assertThat(redisClientProvider.isConectado()).isTrue();
		verify(commands).ping();
	}

	@Test
	@DisplayName("Deve retornar falso quando ocorrer erro no ping")
	void deve_retornar_falso_quando_ocorrer_erro_no_ping() {
		when(connection.isOpen()).thenReturn(true);
		when(connection.sync()).thenReturn(commands);
		doThrow(new RuntimeException("erro")).when(commands).ping();

		ReflectionTestUtils.setField(redisClientProvider, "connection", connection);

		assertThat(redisClientProvider.isConectado()).isFalse();
		verify(commands).ping();
	}

	@Test
	@DisplayName("Deve reconfigurar fechando conexões anteriores se existirem")
	void deve_reconfigurar_fechando_conexoes_anteriores() {
		ReflectionTestUtils.setField(redisClientProvider, "connection", connection);

		try (var redisClientMockedStatic = mockStatic(RedisClient.class)) {
			redisClientMockedStatic.when(() -> RedisClient.create(any(RedisURI.class))).thenReturn(client);
			
			redisClientProvider.reconfigurar();

			verify(connection).close();
			verify(client).shutdown();
		}
	}
	
	@Test
	@DisplayName("Deve criar RedisURI com TLS habilitado")
	void deve_criar_redis_uri_com_tls() {
		when(redisConnectionConfiguration.getTls()).thenReturn(true);

		try (var redisClientMockedStatic = mockStatic(RedisClient.class)) {
			redisClientMockedStatic.when(() -> RedisClient.create(any(RedisURI.class)))
					.thenAnswer(invocation -> {
						RedisURI uri = invocation.getArgument(0);

						assertThat(uri.getHost()).isEqualTo("localhost");
						assertThat(uri.getPort()).isEqualTo(6379);
						assertThat(uri.isSsl()).isTrue();

						var credentials = uri.getCredentialsProvider()
								.resolveCredentials()
								.block();

						assertThat(credentials).isNotNull();
						assertThat(credentials.getUsername()).isNull();

						return client;
					});

			redisClientProvider.reconfigurar();
		}
	}
	
	@Test
	@DisplayName("Deve configurar autenticação com Usuário e Senha se ambos forem informados")
	void deve_configurar_autenticacao_com_usuario_e_senha() {
		when(redisConnectionConfiguration.getUsername()).thenReturn("admin");
		when(redisConnectionConfiguration.getPassword()).thenReturn("segredo");

		try (var redisClientMockedStatic = mockStatic(RedisClient.class)) {
			redisClientMockedStatic.when(() -> RedisClient.create(any(RedisURI.class)))
					.thenAnswer(invocation -> {
						RedisURI uri = invocation.getArgument(0);

						var credentials = uri.getCredentialsProvider()
								.resolveCredentials()
								.block();

						assertThat(credentials).isNotNull();
						assertThat(credentials.getUsername()).isEqualTo("admin");
						assertThat(credentials.getPassword())
								.containsExactly("segredo".toCharArray());

						return client;
					});

			redisClientProvider.reconfigurar();
		}
	}
	
	@Test
	@DisplayName("Deve configurar autenticação apenas com Senha se o usuário for nulo ou em branco")
	void deve_configurar_autenticacao_apenas_com_senha() {
		when(redisConnectionConfiguration.getUsername()).thenReturn("   ");
		when(redisConnectionConfiguration.getPassword()).thenReturn("apenas-senha");

		try (var redisClientMockedStatic = mockStatic(RedisClient.class)) {
			redisClientMockedStatic.when(() -> RedisClient.create(any(RedisURI.class)))
					.thenAnswer(invocation -> {
						RedisURI uri = invocation.getArgument(0);

						var credentials = uri.getCredentialsProvider()
								.resolveCredentials()
								.block();

						assertThat(credentials).isNotNull();
						assertThat(credentials.getUsername()).isNull();
						assertThat(credentials.getPassword())
								.containsExactly("apenas-senha".toCharArray());

						return client;
					});

			redisClientProvider.reconfigurar();
		}
	}
	
	@Test
	@DisplayName("Não deve configurar autenticação quando somente o usuário for informado")
	void nao_deve_configurar_autenticacao_quando_somente_usuario_for_informado() {
		when(redisConnectionConfiguration.getUsername()).thenReturn("admin");
		when(redisConnectionConfiguration.getPassword()).thenReturn(null);

		try (var redisClientMockedStatic = mockStatic(RedisClient.class)) {
			redisClientMockedStatic.when(() -> RedisClient.create(any(RedisURI.class)))
					.thenAnswer(invocation -> {
						RedisURI uri = invocation.getArgument(0);

						var credentials = uri.getCredentialsProvider()
								.resolveCredentials()
								.block();

						assertThat(credentials).isNotNull();
						assertThat(credentials.getUsername()).isNull();

						return client;
					});

			redisClientProvider.reconfigurar();
		}
	}
}