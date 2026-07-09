package br.com.scheiner.aws.console.dynamodb.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.primefaces.PrimeFaces;
import org.springframework.test.util.ReflectionTestUtils;

import br.com.scheiner.aws.console.dynamodb.model.DynamoDbTableMetadata;
import br.com.scheiner.aws.console.dynamodb.service.DynamoDbService;
import br.com.scheiner.aws.console.dynamodb.util.DynamoDbJsonMapper;
import br.com.scheiner.aws.console.web.navigation.ApplicationRoute;
import br.com.scheiner.aws.console.web.navigation.NavigationManager;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;

class DynamoDbControllerTest {

	private DynamoDbService dynamoDbService;

	private NavigationManager navigationManager;

	private DynamoDbController controller;

	@BeforeEach
	void setup() {
		dynamoDbService = mock(DynamoDbService.class);
		navigationManager = mock(NavigationManager.class);
		controller = new DynamoDbController(dynamoDbService, navigationManager);
	}

	private TableDescription criarDescricaoTabela() {

		return TableDescription.builder().tableName("clientes").itemCount(10L).tableStatus("ACTIVE")
				.keySchema(KeySchemaElement.builder().attributeName("id").keyType(KeyType.HASH).build(),
						KeySchemaElement.builder().attributeName("sk").keyType(KeyType.RANGE).build())
				.attributeDefinitions(
						AttributeDefinition.builder().attributeName("id").attributeType(ScalarAttributeType.S).build(),
						AttributeDefinition.builder().attributeName("sk").attributeType(ScalarAttributeType.N).build())
				.build();
	}

	private Map<String, AttributeValue> criarItem() {

		Map<String, AttributeValue> item = new LinkedHashMap<>();

		item.put("id", AttributeValue.builder().s("1").build());
		item.put("sk", AttributeValue.builder().n("10").build());
		item.put("nome", AttributeValue.builder().s("Leandro").build());

		return item;
	}

	private TableDescription criarDescricaoSemSortKey() {

		return TableDescription.builder().tableName("clientes")
				.keySchema(KeySchemaElement.builder().attributeName("id").keyType(KeyType.HASH).build())
				.attributeDefinitions(
						AttributeDefinition.builder().attributeName("id").attributeType(ScalarAttributeType.S).build())
				.build();
	}

	@Test
	@DisplayName("Deve inicializar controller")
	void deve_inicializar_controller() {

		when(dynamoDbService.buscarTabelas()).thenReturn(List.of("clientes"));

		when(navigationManager.getRequestedResource(ApplicationRoute.DYNAMODB)).thenReturn(Optional.empty());

		var facesContext = mock(FacesContext.class);

		try (var mocked = mockStatic(FacesContext.class)) {

			mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			controller.init();

			assertThat(controller.getTabelas()).containsExactly("clientes");

			verify(facesContext).addMessage(isNull(), any(FacesMessage.class));
		}
	}

	@Test
	@DisplayName("Deve atualizar tabelas")
	void deve_atualizar_tabelas() {

		when(dynamoDbService.buscarTabelas()).thenReturn(List.of("clientes", "pedidos"));

		var facesContext = mock(FacesContext.class);

		try (var mocked = mockStatic(FacesContext.class)) {

			mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			controller.atualizarTabelas();

			assertThat(controller.getTabelas()).containsExactly("clientes", "pedidos");

			verify(facesContext).addMessage(isNull(), any(FacesMessage.class));
		}
	}

	@Test
	@DisplayName("Deve tratar erro ao atualizar tabelas")
	void deve_tratar_erro_ao_atualizar_tabelas() {

		when(dynamoDbService.buscarTabelas()).thenThrow(new RuntimeException());

		var facesContext = mock(FacesContext.class);

		try (var mocked = mockStatic(FacesContext.class)) {

			mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			controller.atualizarTabelas();

			verify(facesContext).addMessage(isNull(), any(FacesMessage.class));
		}
	}

	@Test
	@DisplayName("Deve selecionar tabela")
	void deve_selecionar_tabela() {

		when(dynamoDbService.descreverTabela("clientes")).thenReturn(criarDescricaoTabela());

		when(dynamoDbService.buscarItens("clientes")).thenReturn(List.of(criarItem()));

		controller.setTabelaSelecionada("clientes");

		var facesContext = mock(FacesContext.class);

		try (var mocked = mockStatic(FacesContext.class)) {

			mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			controller.selecionarTabela();

			assertThat(controller.getDescricaoTabela()).isNotNull();
			assertThat(controller.getItensTabela()).hasSize(1);
			assertThat(controller.getColunas()).containsExactly("id", "sk", "nome");

			verify(facesContext).addMessage(isNull(), any(FacesMessage.class));
		}
	}

	@Test
	@DisplayName("Deve tratar erro ao selecionar tabela")
	void deve_tratar_erro_ao_selecionar_tabela() {

		when(dynamoDbService.descreverTabela("clientes")).thenThrow(new RuntimeException());

		controller.setTabelaSelecionada("clientes");

		var facesContext = mock(FacesContext.class);

		try (var mocked = mockStatic(FacesContext.class)) {

			mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			controller.selecionarTabela();

			assertThat(controller.getDescricaoTabela()).isNull();
			assertThat(controller.getItensTabela()).isEmpty();
			assertThat(controller.getColunas()).isEmpty();

			verify(facesContext).addMessage(isNull(), any(FacesMessage.class));
		}
	}

	@Test
	@DisplayName("Deve selecionar tabela informada na requisição")
	void deve_selecionar_tabela_informada_na_requisicao() {

		when(dynamoDbService.buscarTabelas()).thenReturn(List.of("clientes"));

		when(navigationManager.getRequestedResource(ApplicationRoute.DYNAMODB)).thenReturn(Optional.of("clientes"));

		when(dynamoDbService.descreverTabela("clientes")).thenReturn(criarDescricaoTabela());

		when(dynamoDbService.buscarItens("clientes")).thenReturn(List.of(criarItem()));

		var facesContext = mock(FacesContext.class);

		try (var mocked = mockStatic(FacesContext.class)) {

			mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			controller.init();

			assertThat(controller.getTabelaSelecionada()).isEqualTo("clientes");
		}
	}

	@Test
	@DisplayName("Deve formatar valores simples de AttributeValue")
	void deve_formatar_valores_simples_de_attribute_value() {
		assertThat(this.controller.formatarValor(AttributeValue.builder().s("texto").build())).isEqualTo("texto");
		assertThat(this.controller.formatarValor(AttributeValue.builder().n("10.5").build())).isEqualTo("10.5");
		assertThat(this.controller.formatarValor(AttributeValue.builder().bool(true).build())).isEqualTo("true");
		assertThat(this.controller.formatarValor(AttributeValue.builder().nul(true).build())).isEmpty();
		assertThat(this.controller.formatarValor(null)).isEmpty();
	}

	@Test
	@DisplayName("Deve manter numero como texto quando nao for numerico")
	void deve_manter_numero_como_texto_quando_nao_for_numerico() {
		assertThat(this.controller.obterValorComparable(AttributeValue.builder().n("NaN-local").build()))
				.isEqualTo("NaN-local");
	}

	@Test
	@DisplayName("Deve identificar valores complexos")
	void deve_identificar_valores_complexos() {
		assertThat(this.controller.isValorComplexo(
				AttributeValue.builder().m(Map.of("a", AttributeValue.builder().s("b").build())).build())).isTrue();
		assertThat(this.controller
				.isValorComplexo(AttributeValue.builder().l(List.of(AttributeValue.builder().s("a").build())).build()))
				.isTrue();
		assertThat(this.controller.isValorComplexo(AttributeValue.builder().ss("a").build())).isTrue();
		assertThat(this.controller.isValorComplexo(AttributeValue.builder().ns("1").build())).isTrue();
		assertThat(this.controller.isValorComplexo(AttributeValue.builder().b(SdkBytes.fromUtf8String("bin")).build()))
				.isTrue();
		assertThat(this.controller.isValorComplexo(AttributeValue.builder().bs(SdkBytes.fromUtf8String("bin")).build()))
				.isTrue();
		assertThat(this.controller.isValorComplexo(AttributeValue.builder().s("simples").build())).isFalse();
		assertThat(this.controller.isValorComplexo(null)).isFalse();
	}

	@Test
	@DisplayName("Deve gerar preview para valores complexos")
	void deve_gerar_preview_para_valores_complexos() {
		assertThat(this.controller.formatarValor(
				AttributeValue.builder().m(Map.of("a", AttributeValue.builder().s("b").build())).build()))
				.isEqualTo("{1 campos}");
		assertThat(this.controller
				.formatarValor(AttributeValue.builder().l(List.of(AttributeValue.builder().s("a").build())).build()))
				.isEqualTo("[1 itens]");
		assertThat(this.controller.formatarValor(AttributeValue.builder().ss("a", "b").build()))
				.isEqualTo("[2 strings]");
		assertThat(this.controller.formatarValor(AttributeValue.builder().ns("1", "2").build()))
				.isEqualTo("[2 números]");
		assertThat(this.controller.formatarValor(AttributeValue.builder().b(SdkBytes.fromUtf8String("bin")).build()))
				.isEqualTo("[binário]");
		assertThat(this.controller.formatarValor(
				AttributeValue.builder().bs(SdkBytes.fromUtf8String("a"), SdkBytes.fromUtf8String("b")).build()))
				.isEqualTo("[2 binários]");
	}

	@Test
	@DisplayName("Deve expor nulos quando nenhuma tabela estiver selecionada")
	void deve_expor_nulos_quando_nenhuma_tabela_estiver_selecionada() {
		assertThat(this.controller.getPartitionKey()).isNull();
		assertThat(this.controller.getSortKey()).isNull();
		assertThat(this.controller.getTipoPartitionKey()).isNull();
		assertThat(this.controller.getTipoSortKey()).isNull();
		assertThat(this.controller.getQuantidadeItens()).isNull();
		assertThat(this.controller.getStatusTabela()).isNull();
	}

	@Test
	@DisplayName("Deve atualizar itens da tabela")
	void deve_atualizar_itens_da_tabela() {

		when(dynamoDbService.buscarItens("clientes")).thenReturn(List.of(criarItem()));

		when(dynamoDbService.descreverTabela("clientes")).thenReturn(criarDescricaoTabela());

		controller.setTabelaSelecionada("clientes");

		var facesContext = mock(FacesContext.class);

		try (var mocked = mockStatic(FacesContext.class)) {

			mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			controller.selecionarTabela();
			controller.atualizarItens();

			assertThat(controller.getItensTabela()).hasSize(1);
			assertThat(controller.getColunas()).containsExactly("id", "sk", "nome");

			verify(facesContext, atLeastOnce()).addMessage(isNull(), any(FacesMessage.class));
		}
	}

	@Test
	@DisplayName("Deve tratar erro ao atualizar itens")
	void deve_tratar_erro_ao_atualizar_itens() {

		when(dynamoDbService.buscarItens("clientes")).thenThrow(new RuntimeException());

		controller.setTabelaSelecionada("clientes");

		var facesContext = mock(FacesContext.class);

		try (var mocked = mockStatic(FacesContext.class)) {

			mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			controller.atualizarItens();

			verify(facesContext).addMessage(isNull(), any(FacesMessage.class));
		}
	}

	@Test
	@DisplayName("Deve criar novo item utilizando metadados da tabela")
	void deve_criar_novo_item_utilizando_metadados_da_tabela() {

		when(dynamoDbService.descreverTabela("clientes")).thenReturn(criarDescricaoTabela());

		when(dynamoDbService.buscarItens("clientes")).thenReturn(List.of());

		controller.setTabelaSelecionada("clientes");

		var facesContext = mock(FacesContext.class);

		try (var mocked = mockStatic(FacesContext.class)) {

			mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			controller.selecionarTabela();
			controller.novoItem();

			assertThat(controller.isNovoItem()).isTrue();
			assertThat(controller.getJsonItemFormulario()).contains("\"id\"");
			assertThat(controller.getJsonItemFormulario()).contains("\"sk\"");
		}
	}

	@Test
	@DisplayName("Deve visualizar valor complexo")
	void deve_visualizar_valor_complexo() {

		var valor = AttributeValue.builder().m(Map.of("nome", AttributeValue.builder().s("Leandro").build())).build();

		controller.visualizarValorComplexo(valor);

		assertThat(controller.getJsonValorVisualizacao()).contains("nome").contains("Leandro");
	}

	@Test
	@DisplayName("Deve editar item existente")
	void deve_editar_item_existente() {

		when(dynamoDbService.descreverTabela("clientes")).thenReturn(criarDescricaoTabela());

		var item = criarItem();

		when(dynamoDbService.buscarItens("clientes")).thenReturn(List.of(item));

		when(dynamoDbService.buscarItem(any(), any())).thenReturn(item);

		controller.setTabelaSelecionada("clientes");

		var facesContext = mock(FacesContext.class);

		try (var mocked = mockStatic(FacesContext.class)) {

			mocked.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			controller.selecionarTabela();
			controller.editarItem(item);

			assertThat(controller.isNovoItem()).isFalse();
			assertThat(controller.getJsonItemFormulario()).contains("Leandro");
		}
	}

	@Test
	@DisplayName("Deve salvar novo item")
	void deve_salvar_novo_item() {

		when(dynamoDbService.descreverTabela("clientes")).thenReturn(criarDescricaoTabela());

		when(dynamoDbService.buscarItens("clientes")).thenReturn(List.of());

		controller.setTabelaSelecionada("clientes");

		var facesContext = mock(FacesContext.class);
		var primeFaces = mock(PrimeFaces.class);
		var ajax = mock(PrimeFaces.Ajax.class);

		Map<String, AttributeValue> item = criarItem();

		try (var mockedFaces = mockStatic(FacesContext.class);
				var mockedPrimeFaces = mockStatic(PrimeFaces.class);
				var mockedMapper = mockStatic(DynamoDbJsonMapper.class)) {

			mockedFaces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			mockedPrimeFaces.when(PrimeFaces::current).thenReturn(primeFaces);

			when(primeFaces.ajax()).thenReturn(ajax);

			mockedMapper.when(() -> DynamoDbJsonMapper.fromJson(any())).thenReturn(item);

			controller.selecionarTabela();
			controller.novoItem();
			controller.setJsonItemFormulario("{}");

			controller.salvarItem();

			verify(dynamoDbService).salvarItem("clientes", item);

			verify(ajax).addCallbackParam("salvo", true);
		}
	}

	@Test
	@DisplayName("Deve tratar erro ao excluir item")
	void deve_tratar_erro_ao_excluir_item() {

		doThrow(new RuntimeException()).when(dynamoDbService).excluirItem(any(), any());

		controller.setTabelaSelecionada("clientes");

		var facesContext = mock(FacesContext.class);

		try (var mockedFaces = mockStatic(FacesContext.class)) {

			mockedFaces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			controller.excluirItem(criarItem());

			verify(facesContext).addMessage(isNull(), any(FacesMessage.class));
		}
	}

	@Test
	@DisplayName("Deve salvar novo item com sucesso")
	void deve_salvar_novo_item_com_sucesso() {

		when(dynamoDbService.descreverTabela("clientes")).thenReturn(criarDescricaoTabela());

		when(dynamoDbService.buscarItens("clientes")).thenReturn(List.of());

		controller.setTabelaSelecionada("clientes");

		var facesContext = mock(FacesContext.class);

		var primeFaces = mock(PrimeFaces.class);
		var ajax = mock(PrimeFaces.Ajax.class);

		Map<String, AttributeValue> item = criarItem();

		try (var mockedFacesContext = mockStatic(FacesContext.class);
				var mockedPrimeFaces = mockStatic(PrimeFaces.class);
				var mockedMapper = mockStatic(DynamoDbJsonMapper.class)) {

			mockedFacesContext.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			mockedPrimeFaces.when(PrimeFaces::current).thenReturn(primeFaces);

			when(primeFaces.ajax()).thenReturn(ajax);

			mockedMapper.when(() -> DynamoDbJsonMapper.fromJson(any())).thenReturn(item);

			controller.selecionarTabela();

			controller.novoItem();

			controller.setJsonItemFormulario("{}");

			controller.salvarItem();

			verify(dynamoDbService).salvarItem("clientes", item);

			verify(ajax).addCallbackParam("salvo", true);

			verify(facesContext, atLeastOnce()).addMessage(isNull(), any(FacesMessage.class));

			assertThat(controller.isNovoItem()).isTrue();
		}
	}

	@Test
	@DisplayName("Deve excluir item com sucesso")
	void deve_excluir_item_com_sucesso() {

		Map<String, AttributeValue> item = criarItem();

		when(dynamoDbService.descreverTabela("clientes")).thenReturn(criarDescricaoTabela());

		when(dynamoDbService.buscarItens("clientes")).thenReturn(List.of(item));

		var facesContext = mock(FacesContext.class);

		try (var mockedFacesContext = mockStatic(FacesContext.class)) {

			mockedFacesContext.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			controller.setTabelaSelecionada("clientes");

			controller.selecionarTabela();

			controller.excluirItem(item);

			verify(dynamoDbService).excluirItem(eq("clientes"),
					argThat(chave -> "1".equals(chave.get("id").s()) && "10".equals(chave.get("sk").n())));

			verify(dynamoDbService, atLeastOnce()).buscarItens("clientes");

			verify(facesContext, atLeastOnce()).addMessage(isNull(), any(FacesMessage.class));

			assertThat(controller.getItensTabela()).isNotNull();
		}
	}

	@Test
	@DisplayName("Deve excluir chave antiga quando item editado alterar a chave")
	void deve_excluir_chave_antiga_quando_item_editado_alterar_a_chave() {

		when(dynamoDbService.descreverTabela("clientes")).thenReturn(criarDescricaoTabela());

		when(dynamoDbService.buscarItens("clientes")).thenReturn(List.of(criarItem()));

		Map<String, AttributeValue> itemOriginal = criarItem();

		Map<String, AttributeValue> itemAlterado = new LinkedHashMap<>();
		itemAlterado.put("id", AttributeValue.builder().s("2").build());
		itemAlterado.put("sk", AttributeValue.builder().n("20").build());
		itemAlterado.put("nome", AttributeValue.builder().s("Novo").build());

		controller.setTabelaSelecionada("clientes");

		var facesContext = mock(FacesContext.class);
		var primeFaces = mock(PrimeFaces.class);
		var ajax = mock(PrimeFaces.Ajax.class);

		try (var mockedFaces = mockStatic(FacesContext.class);
				var mockedPrime = mockStatic(PrimeFaces.class);
				var mockedMapper = mockStatic(DynamoDbJsonMapper.class)) {

			mockedFaces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			mockedPrime.when(PrimeFaces::current).thenReturn(primeFaces);

			when(primeFaces.ajax()).thenReturn(ajax);

			mockedMapper.when(() -> DynamoDbJsonMapper.fromJson(any())).thenReturn(itemAlterado);

			controller.selecionarTabela();
			controller.editarItem(itemOriginal);

			controller.setJsonItemFormulario("{}");

			controller.salvarItem();

			verify(dynamoDbService).salvarItem("clientes", itemAlterado);

			verify(dynamoDbService).excluirItem(eq("clientes"),
					argThat(chave -> "1".equals(chave.get("id").s()) && "10".equals(chave.get("sk").n())));

			verify(ajax).addCallbackParam("salvo", true);
		}
	}

	@Test
	@DisplayName("Não deve excluir chave quando item editado mantiver a mesma chave")
	void nao_deve_excluir_chave_quando_item_editado_manter_mesma_chave() {

		when(dynamoDbService.descreverTabela("clientes")).thenReturn(criarDescricaoTabela());

		when(dynamoDbService.buscarItens("clientes")).thenReturn(List.of(criarItem()));

		Map<String, AttributeValue> item = criarItem();

		controller.setTabelaSelecionada("clientes");

		var facesContext = mock(FacesContext.class);
		var primeFaces = mock(PrimeFaces.class);
		var ajax = mock(PrimeFaces.Ajax.class);

		try (var mockedFaces = mockStatic(FacesContext.class);
				var mockedPrime = mockStatic(PrimeFaces.class);
				var mockedMapper = mockStatic(DynamoDbJsonMapper.class)) {

			mockedFaces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			mockedPrime.when(PrimeFaces::current).thenReturn(primeFaces);

			when(primeFaces.ajax()).thenReturn(ajax);

			mockedMapper.when(() -> DynamoDbJsonMapper.fromJson(any())).thenReturn(item);

			controller.selecionarTabela();
			controller.editarItem(item);

			controller.setJsonItemFormulario("{}");

			controller.salvarItem();

			verify(dynamoDbService).salvarItem("clientes", item);

			verify(dynamoDbService, never()).excluirItem(any(), any());

			verify(ajax).addCallbackParam("salvo", true);
		}
	}

	@Test
	@DisplayName("Deve tratar erro ao salvar item")
	void deve_tratar_erro_ao_salvar_item() {

		when(dynamoDbService.descreverTabela("clientes")).thenReturn(criarDescricaoTabela());

		when(dynamoDbService.buscarItens("clientes")).thenReturn(List.of());

		controller.setTabelaSelecionada("clientes");

		Map<String, AttributeValue> item = criarItem();

		var facesContext = mock(FacesContext.class);
		var primeFaces = mock(PrimeFaces.class);
		var ajax = mock(PrimeFaces.Ajax.class);

		try (var mockedFaces = mockStatic(FacesContext.class);
				var mockedPrime = mockStatic(PrimeFaces.class);
				var mockedMapper = mockStatic(DynamoDbJsonMapper.class)) {

			mockedFaces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			mockedPrime.when(PrimeFaces::current).thenReturn(primeFaces);

			when(primeFaces.ajax()).thenReturn(ajax);

			mockedMapper.when(() -> DynamoDbJsonMapper.fromJson(any())).thenReturn(item);

			doThrow(new RuntimeException()).when(dynamoDbService).salvarItem(any(), any());

			controller.selecionarTabela();
			controller.novoItem();

			controller.setJsonItemFormulario("{}");

			controller.salvarItem();

			verify(ajax).addCallbackParam("salvo", false);

			verify(facesContext, atLeastOnce()).addMessage(isNull(), any(FacesMessage.class));
		}
	}

	@Test
	@DisplayName("Deve alterar lista de tabelas")
	void deve_alterar_lista_de_tabelas() {

		controller.setTabelas(List.of("clientes", "pedidos"));

		assertThat(controller.getTabelas()).containsExactly("clientes", "pedidos");
	}

	@Test
	@DisplayName("Deve retornar vazio quando AttributeValue não possuir valor")
	void deve_retornar_vazio_quando_attribute_value_nao_possuir_valor() {

		assertThat(controller.formatarValor(AttributeValue.builder().build())).isEmpty();
	}

	@Test
	@DisplayName("Deve formatar conjunto de binários")
	void deve_formatar_conjunto_de_binarios() {

		var valor = AttributeValue.builder()
				.bs(SdkBytes.fromUtf8String("a"), SdkBytes.fromUtf8String("b"), SdkBytes.fromUtf8String("c")).build();

		assertThat(controller.formatarValor(valor)).isEqualTo("[3 binários]");
	}

	@Test
	@DisplayName("Deve criar item apenas com chave de partição")
	void deve_criar_item_apenas_com_partition_key() {

		when(dynamoDbService.descreverTabela("clientes")).thenReturn(criarDescricaoSemSortKey());

		when(dynamoDbService.buscarItens("clientes")).thenReturn(List.of());

		var facesContext = mock(FacesContext.class);

		try (var mockedFacesContext = mockStatic(FacesContext.class)) {

			mockedFacesContext.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			controller.setTabelaSelecionada("clientes");

			controller.selecionarTabela();

			controller.novoItem();

			assertThat(controller.getJsonItemFormulario()).contains("\"id\"").doesNotContain("\"sk\"");

			verify(facesContext, atLeastOnce()).addMessage(isNull(), any(FacesMessage.class));
		}
	}

	@Test
	@DisplayName("Deve ordenar colunas colocando chaves primeiro")
	void deve_ordenar_colunas_colocando_chaves_primeiro() {

		when(dynamoDbService.descreverTabela("clientes")).thenReturn(criarDescricaoTabela());

		when(dynamoDbService.buscarItens("clientes")).thenReturn(List.of(criarItem()));

		var facesContext = mock(FacesContext.class);

		try (var mockedFacesContext = mockStatic(FacesContext.class)) {

			mockedFacesContext.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

			controller.setTabelaSelecionada("clientes");

			controller.selecionarTabela();

			assertThat(controller.getColunas()).containsExactly("id", "sk", "nome");

			verify(facesContext, atLeastOnce()).addMessage(isNull(), any(FacesMessage.class));
		}
	}
	
	@Test
	@DisplayName("Deve retornar metadados da tabela")
	void deve_retornar_metadados_da_tabela() {

		when(dynamoDbService.descreverTabela("clientes"))
				.thenReturn(criarDescricaoTabela());

		when(dynamoDbService.buscarItens("clientes"))
				.thenReturn(List.of());

		var facesContext = mock(FacesContext.class);

		try (MockedStatic<FacesContext> mockedFacesContext = mockStatic(FacesContext.class)) {

			mockedFacesContext.when(FacesContext::getCurrentInstance)
					.thenReturn(facesContext);

			controller.setTabelaSelecionada("clientes");

			controller.selecionarTabela();

			assertThat(controller.getPartitionKey()).isEqualTo("id");
			assertThat(controller.getSortKey()).isEqualTo("sk");
			assertThat(controller.getTipoPartitionKey()).isEqualTo("S");
			assertThat(controller.getTipoSortKey()).isEqualTo("N");
			assertThat(controller.getQuantidadeItens()).isEqualTo(10L);
			assertThat(controller.getStatusTabela()).isEqualTo("ACTIVE");

			verify(facesContext, atLeastOnce())
					.addMessage(isNull(), any(FacesMessage.class));
		}
	}
	
	@Test
	@DisplayName("Não deve selecionar tabela inexistente")
	void nao_deve_selecionar_tabela_inexistente() {

		controller.setTabelas(List.of("clientes"));

		ReflectionTestUtils.invokeMethod(
				controller,
				"selecionarTabelaPorNome",
				"pedidos");

		assertThat(controller.getTabelaSelecionada()).isNull();
	}
	
	@Test
	@DisplayName("Deve montar colunas quando item não possuir sort key")
	void deve_montar_colunas_quando_item_nao_possuir_sort_key() {

		when(dynamoDbService.descreverTabela("clientes"))
				.thenReturn(criarDescricaoTabela());

		Map<String, AttributeValue> item = new LinkedHashMap<>();
		item.put("id", AttributeValue.builder().s("1").build());

		when(dynamoDbService.buscarItens("clientes"))
				.thenReturn(List.of(item));

		var facesContext = mock(FacesContext.class);

		try (MockedStatic<FacesContext> mocked = mockStatic(FacesContext.class)) {

			mocked.when(FacesContext::getCurrentInstance)
					.thenReturn(facesContext);

			controller.setTabelaSelecionada("clientes");

			controller.selecionarTabela();

			assertThat(controller.getColunas())
					.containsExactly("id");
		}
	}
	
	@Test
	@DisplayName("Deve criar item quando existir somente sort key")
	void deve_criar_item_quando_existir_somente_sort_key() {

		var metadata = mock(DynamoDbTableMetadata.class);

		when(metadata.getPartitionKey()).thenReturn(null);
		when(metadata.getSortKey()).thenReturn("sk");
		when(metadata.getTipoAtributo("sk")).thenReturn("N");

		ReflectionTestUtils.setField(controller, "metadataTabela", metadata);

		controller.novoItem();

		assertThat(controller.getJsonItemFormulario())
				.contains("\"sk\"");
	}
	
	@Test
	@DisplayName("Deve extrair somente sort key")
	void deve_extrair_somente_sort_key() {

		var metadata = mock(DynamoDbTableMetadata.class);

		when(metadata.getPartitionKey()).thenReturn(null);
		when(metadata.getSortKey()).thenReturn("sk");

		ReflectionTestUtils.setField(controller, "metadataTabela", metadata);
		ReflectionTestUtils.setField(controller, "tabelaSelecionada", "clientes");

		Map<String, AttributeValue> item = new LinkedHashMap<>();
		item.put("sk", AttributeValue.builder().n("10").build());

		when(dynamoDbService.buscarItem(any(), any()))
				.thenReturn(item);

		controller.editarItem(item);

		verify(dynamoDbService).buscarItem(
				eq("clientes"),
				argThat(chave ->
						chave.size() == 1 &&
						chave.containsKey("sk")));
	}
	
	@Test
	@DisplayName("Deve extrair somente partition key")
	void deve_extrair_somente_partition_key() {

		var metadata = mock(DynamoDbTableMetadata.class);

		when(metadata.getPartitionKey()).thenReturn("id");
		when(metadata.getSortKey()).thenReturn(null);

		ReflectionTestUtils.setField(controller, "metadataTabela", metadata);
		ReflectionTestUtils.setField(controller, "tabelaSelecionada", "clientes");

		Map<String, AttributeValue> item = new LinkedHashMap<>();
		item.put("id", AttributeValue.builder().s("1").build());

		when(dynamoDbService.buscarItem(any(), any()))
				.thenReturn(item);

		controller.editarItem(item);

		verify(dynamoDbService).buscarItem(
				eq("clientes"),
				argThat(chave ->
						chave.size() == 1 &&
						chave.containsKey("id")));
	}
	
	@Test
	@DisplayName("Deve montar colunas quando item não possuir a partition key")
	void deve_montar_colunas_quando_item_nao_possuir_partition_key() {

		when(dynamoDbService.descreverTabela("clientes"))
				.thenReturn(criarDescricaoTabela());

		Map<String, AttributeValue> item = new LinkedHashMap<>();
		item.put("nome", AttributeValue.builder().s("Leandro").build());

		when(dynamoDbService.buscarItens("clientes"))
				.thenReturn(List.of(item));

		var facesContext = mock(FacesContext.class);

		try (var mockedFaces = mockStatic(FacesContext.class)) {

			mockedFaces.when(FacesContext::getCurrentInstance)
					.thenReturn(facesContext);

			controller.setTabelaSelecionada("clientes");

			controller.selecionarTabela();

			assertThat(controller.getColunas())
					.containsExactly("nome");
		}
	}
	
	@Test
	@DisplayName("Deve montar colunas quando a tabela não possuir partition key nem sort key")
	void deve_montar_colunas_quando_tabela_nao_possuir_partition_key_nem_sort_key() {

		var descricaoSemChaves = software.amazon.awssdk.services.dynamodb.model.TableDescription.builder()
	            .tableName("clientes")
	            .keySchema(java.util.List.of())
	            .build();

	    when(dynamoDbService.descreverTabela("clientes")).thenReturn(descricaoSemChaves);

	    Map<String, AttributeValue> item = new java.util.LinkedHashMap<>();
	    item.put("nome", AttributeValue.builder().s("Leandro").build());
	    
	    when(dynamoDbService.buscarItens("clientes")).thenReturn(List.of(item));

	    var facesContext = mock(FacesContext.class);

	    try (var mockedFaces = mockStatic(FacesContext.class)) {
	        mockedFaces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

	        controller.setTabelaSelecionada("clientes");
	        controller.selecionarTabela();

	        assertThat(controller.getColunas()).containsExactly("nome");
	    }
	}
}
