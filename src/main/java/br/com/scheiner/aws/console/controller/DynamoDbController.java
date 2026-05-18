package br.com.scheiner.aws.console.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.primefaces.PrimeFaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.scheiner.aws.console.model.DynamoDbTableMetadata;
import br.com.scheiner.aws.console.service.DynamodbService;
import br.com.scheiner.aws.console.utils.DynamoDbJsonMapper;
import br.com.scheiner.aws.console.utils.JsonUtils;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;

@Named
@ViewScoped
public class DynamoDbController implements SqsController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DynamoDbController.class);

    private final DynamodbService dynamodbService;

    private List<String> tabelas;

    private String tabelaSelecionada;

    private TableDescription descricaoTabela;

    private DynamoDbTableMetadata metadataTabela;

    private List<Map<String, AttributeValue>> itensTabela = List.of();

    private List<String> colunas = List.of();

    private String jsonItemFormulario;

    private String jsonValorVisualizacao;

    private boolean novoItem;

    private Map<String, AttributeValue> chaveOriginalEmEdicao = new LinkedHashMap<>();
    
    public DynamoDbController(DynamodbService dynamodbService) {
		super();
		this.dynamodbService = dynamodbService;
        this.atualizarTabelas();
	}

    public void atualizarTabelas() {

        try {

        	this.tabelas = this.dynamodbService.buscarTabelas();
        	this.limparTabelaSelecionada();
            this.adicionarMensagem(FacesMessage.SEVERITY_INFO, "Sucesso", "Tabelas carregadas com sucesso." );

        } catch (Exception e) {
			LOGGER.error("Erro carregando tabelas", e);
            this.adicionarMensagem( FacesMessage.SEVERITY_ERROR, "Erro",  "Erro ao carregar tabelas." );
        }
    }

    public void selecionarTabela() {
    	try {
    		this.descricaoTabela = dynamodbService.descreverTabela(this.tabelaSelecionada);
    		this.metadataTabela = new DynamoDbTableMetadata(this.descricaoTabela);
    		this.itensTabela = this.dynamodbService.buscarItens(this.tabelaSelecionada);
    		this.colunas = this.montarColunas(this.itensTabela);
    		adicionarMensagem(FacesMessage.SEVERITY_INFO, "Tabela selecionada", this.tabelaSelecionada);
    	} catch (Exception e) {
    		LOGGER.error("Erro carregando detalhes da tabela {}", this.tabelaSelecionada, e);
    		this.limparDetalhesTabela();
    		adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Erro", "Erro ao carregar detalhes da tabela.");
    	}
    }

    public void editarItem(Map<String, AttributeValue> item) {
    	var chaveItem = extrairChave(item);
    	this.chaveOriginalEmEdicao = chaveItem;
    	var itemCompleto = dynamodbService.buscarItem(
    			this.tabelaSelecionada,
    			this.chaveOriginalEmEdicao
    	);
    	this.jsonItemFormulario = JsonUtils.prettyPrint(DynamoDbJsonMapper.toJson(itemCompleto));
    	this.novoItem = false;
    }

    public void novoItem() {
    	Map<String, AttributeValue> itemInicial = new LinkedHashMap<>();
    	var partitionKey = this.metadataTabela.getPartitionKey();
    	var sortKey = this.metadataTabela.getSortKey();

    	if (partitionKey != null) {
    		itemInicial.put(partitionKey, criarValorVazioPorTipo(this.metadataTabela.getTipoAtributo(partitionKey)));
    	}
    	if (sortKey != null) {
    		itemInicial.put(sortKey, criarValorVazioPorTipo(this.metadataTabela.getTipoAtributo(sortKey)));
    	}

    	this.jsonItemFormulario = JsonUtils.prettyPrint(DynamoDbJsonMapper.toJson(itemInicial));
    	this.chaveOriginalEmEdicao = new LinkedHashMap<>();
    	this.novoItem = true;
    }

    public void salvarItem() {
    	try {
    		var itemParaSalvar = DynamoDbJsonMapper.fromJson(this.jsonItemFormulario);
    		this.dynamodbService.salvarItem(this.tabelaSelecionada, itemParaSalvar);
    		var novaChave = extrairChave(itemParaSalvar);
    		
    		if (!this.novoItem && !novaChave.equals(this.chaveOriginalEmEdicao)) {
    			this.dynamodbService.excluirItem(this.tabelaSelecionada, this.chaveOriginalEmEdicao);
    		}
    		
    		this.recarregarItensTabela();
    		PrimeFaces.current().ajax().addCallbackParam("salvo", true);
    		
    		adicionarMensagem(
    				FacesMessage.SEVERITY_INFO,
    				this.novoItem ? "Item criado" : "Item salvo",
    				this.novoItem ? "Item criado com sucesso." : "Item atualizado com sucesso."
    		);
    		
    	} catch (Exception e) {
    		LOGGER.error("Erro salvando item da tabela {}", this.tabelaSelecionada, e);
    		PrimeFaces.current().ajax().addCallbackParam("salvo", false);
    		adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Erro", "Erro ao salvar item.");
    	}
    }

    public void excluirItem(Map<String, AttributeValue> item) {
    	try {
    		this.dynamodbService.excluirItem(
    				this.tabelaSelecionada,
    				extrairChave(item)
    		);
    		this.recarregarItensTabela();
    		adicionarMensagem(FacesMessage.SEVERITY_INFO, "Item excluído", "Item removido com sucesso.");
    	} catch (Exception e) {
    		LOGGER.error("Erro excluindo item da tabela {}", this.tabelaSelecionada, e);
    		adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Erro", "Erro ao excluir item.");
    	}
    }

    public void atualizarItens() {
    	try {
    		this.recarregarItensTabela();
    		adicionarMensagem(FacesMessage.SEVERITY_INFO, "Itens atualizados", "Itens recarregados com sucesso.");
    	} catch (Exception e) {
    		LOGGER.error("Erro atualizando itens da tabela {}", this.tabelaSelecionada, e);
    		adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Erro", "Erro ao atualizar itens.");
    	}
    }

    public List<String> getTabelas() {
        return tabelas;
    }

    public void setTabelas(List<String> tabelas) {
        this.tabelas = tabelas;
    }

    public String getTabelaSelecionada() {
        return tabelaSelecionada;
    }

    public void setTabelaSelecionada(String tabelaSelecionada) {
        this.tabelaSelecionada = tabelaSelecionada;
    }

    private void limparTabelaSelecionada() {
    	this.tabelaSelecionada = null;
    	this.limparDetalhesTabela();
    }

    private void limparDetalhesTabela() {
    	this.descricaoTabela = null;
    	this.metadataTabela = null;
    	this.itensTabela = List.of();
    	this.colunas = List.of();
    	this.jsonItemFormulario = null;
    	this.jsonValorVisualizacao = null;
    	this.novoItem = false;
    	this.chaveOriginalEmEdicao = new LinkedHashMap<>();
    }

    private List<String> montarColunas(List<Map<String, AttributeValue>> itens) {
    	Set<String> colunasEncontradas = new LinkedHashSet<>();
    	itens.forEach(item -> colunasEncontradas.addAll(item.keySet()));

    	List<String> colunasOrdenadas = new ArrayList<>();
    	var partitionKey = this.metadataTabela.getPartitionKey();
    	var sortKey = this.metadataTabela.getSortKey();

    	if (partitionKey != null && colunasEncontradas.remove(partitionKey)) {
    		colunasOrdenadas.add(partitionKey);
    	}

    	if (sortKey != null && colunasEncontradas.remove(sortKey)) {
    		colunasOrdenadas.add(sortKey);
    	}

    	colunasOrdenadas.addAll(colunasEncontradas);
    	return colunasOrdenadas;
    }

    private void recarregarItensTabela() {
    	this.itensTabela = this.dynamodbService.buscarItens(this.tabelaSelecionada);
    	this.colunas = montarColunas(this.itensTabela);
    }

    private Map<String, AttributeValue> extrairChave(Map<String, AttributeValue> item) {
    	Map<String, AttributeValue> chave = new LinkedHashMap<>();
    	var partitionKey = this.metadataTabela.getPartitionKey();
    	var sortKey = this.metadataTabela.getSortKey();

    	if (partitionKey != null) {
    		chave.put(partitionKey, item.get(partitionKey));
    	}
    	if (sortKey != null) {
    		chave.put(sortKey, item.get(sortKey));
    	}

    	return chave;
    }

    public String getPartitionKey() {
    	return metadataTabela != null ? metadataTabela.getPartitionKey() : null;
    }

    public String getSortKey() {
    	return metadataTabela != null ? metadataTabela.getSortKey() : null;
    }

    public String getTipoPartitionKey() {
    	return metadataTabela != null ? metadataTabela.getTipoPartitionKey() : null;
    }

    public String getTipoSortKey() {
    	return metadataTabela != null ? metadataTabela.getTipoSortKey() : null;
    }

    public TableDescription getDescricaoTabela() {
		return descricaoTabela;
	}

	public Long getQuantidadeItens() {
		return metadataTabela != null ? metadataTabela.getQuantidadeItens() : null;
	}

	public String getStatusTabela() {
		return metadataTabela != null ? metadataTabela.getStatusTabela() : null;
	}

	public List<Map<String, AttributeValue>> getItensTabela() {
		return itensTabela;
	}

	public List<String> getColunas() {
		return colunas;
	}

	public String getJsonItemFormulario() {
		return jsonItemFormulario;
	}

	public void setJsonItemFormulario(String jsonItemFormulario) {
		this.jsonItemFormulario = jsonItemFormulario;
	}

	public boolean isNovoItem() {
		return novoItem;
	}

	public String formatarValor(AttributeValue valor) {
		if (valor == null) {
			return "";
		}
		if (valor.s() != null) {
			return valor.s();
		}
		if (valor.n() != null) {
			return valor.n();
		}
		if (valor.bool() != null) {
			return valor.bool().toString();
		}
		if (Boolean.TRUE.equals(valor.nul())) {
			return "null";
		}

		return this.formatarPreviewValorComplexo(valor);
	}

	public boolean isValorComplexo(AttributeValue valor) {
		return valor != null
				&& (valor.hasM() || valor.hasL() || valor.hasSs() || valor.hasNs() || valor.b() != null || valor.hasBs());
	}

	public void visualizarValorComplexo(AttributeValue valor) {
		this.jsonValorVisualizacao = JsonUtils.prettyPrint(DynamoDbJsonMapper.toJson(valor));
	}

	public String getJsonValorVisualizacao() {
		return jsonValorVisualizacao;
	}

	private AttributeValue criarValorVazioPorTipo(String tipo) {
		if ("N".equals(tipo)) {
			return AttributeValue.builder().n("0").build();
		}

		return AttributeValue.builder().s("").build();
	}

	private String formatarPreviewValorComplexo(AttributeValue valor) {
		if (valor.hasM()) {
			return "{%d campos}".formatted(valor.m().size());
		}
		if (valor.hasL()) {
			return "[%d itens]".formatted(valor.l().size());
		}
		if (valor.hasSs()) {
			return "[%d strings]".formatted(valor.ss().size());
		}
		if (valor.hasNs()) {
			return "[%d números]".formatted(valor.ns().size());
		}
		if (valor.b() != null) {
			return "[binário]";
		}
		if (valor.hasBs()) {
			return "[%d binários]".formatted(valor.bs().size());
		}

		return "";
	}
}
