package br.com.scheiner.aws.console.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.scheiner.aws.console.service.DynamodbService;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

@Named
@ViewScoped
public class DynamoDbController implements SqsController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DynamoDbController.class);

    private final DynamodbService dynamodbService;

    private List<String> tabelas;

    private String tabelaSelecionada;
    
    public DynamoDbController(DynamodbService dynamodbService) {
		super();
		this.dynamodbService = dynamodbService;
        atualizarTabelas();
	}

    public void atualizarTabelas() {

        try {

        	this.tabelas = dynamodbService.buscarTabelas();
        	this.limparTabelaSelecionada();
            adicionarMensagem(FacesMessage.SEVERITY_INFO, "Sucesso", "Tabelas carregadas com sucesso." );

        } catch (Exception e) {
			LOGGER.error("Erro carregando tabelas", e);
            adicionarMensagem( FacesMessage.SEVERITY_ERROR, "Erro",  "Erro ao carregar tabelas." );
        }
    }

    public void selecionarTabela() {
          adicionarMensagem(FacesMessage.SEVERITY_INFO,"Tabela selecionada", this.tabelaSelecionada );
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
    }
}