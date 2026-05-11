# SQS JSF Console

Aplicação web para interação visual com filas AWS SQS utilizando Java, JSF, PrimeFaces e LocalStack.

O projeto fornece uma interface administrativa para gerenciamento e inspeção de filas SQS em ambiente local, permitindo executar operações diretamente pela interface web.

---

# Visão geral

O sistema foi desenvolvido para centralizar operações comuns de mensageria SQS em uma única interface.

A aplicação permite:

- listar filas
- publicar mensagens
- visualizar mensagens
- inspecionar payloads
- acompanhar leituras das mensagens
- executar purge de filas
- interagir com LocalStack
- administrar filas de forma visual

A proposta do projeto é fornecer uma experiência simples e direta para testes, estudos e troubleshooting utilizando AWS SQS localmente.

---

# Arquitetura do projeto

O projeto foi desenvolvido utilizando:

- Java 21
- Spring Boot 4
- JoinFaces
- PrimeFaces
- AWS SDK v2
- LocalStack
- Docker

A aplicação utiliza JSF como camada de interface e o AWS SDK v2 para comunicação com o SQS.

O ambiente local é executado através do LocalStack via Docker.

---

# Estrutura do projeto

```text
src/main/java
 └── br/com/scheiner/sqs/console
      ├── config
      ├── controller
      ├── provider
      ├── service
      └── model
```

## config

Contém configurações da aplicação e integração com o ambiente AWS/LocalStack.

## controller

Responsável pela interação da interface JSF com as regras da aplicação.

Os controllers controlam:

- carregamento das filas
- visualização de mensagens
- publicação
- purge
- atualização da interface

## provider

Responsável pela criação e gerenciamento do client AWS SQS.

## service

Contém operações relacionadas ao SQS:

- envio de mensagens
- leitura de mensagens
- purge
- consulta de filas

## model

Objetos utilizados pela camada de apresentação.

---

# Interface da aplicação

A interface foi construída utilizando JSF + PrimeFaces.

O sistema possui uma abordagem visual e operacional, permitindo interagir diretamente com as filas.

Entre as funcionalidades disponíveis:

## Listagem de filas

A aplicação apresenta as filas disponíveis configuradas no ambiente.

## Publicação de mensagens

Mensagens podem ser enviadas diretamente pela interface.

O sistema permite:

- selecionar fila
- informar payload
- publicar mensagens no SQS

## Visualização de mensagens

As mensagens podem ser visualizadas diretamente pela interface.

A tela exibe:

- Message ID
- payload
- quantidade de leituras
- detalhes da mensagem

## Purge de filas

A aplicação permite limpar filas diretamente pela interface.

## Dialogs de visualização

Os detalhes das mensagens podem ser visualizados em dialogs PrimeFaces.

---

# Integração com LocalStack

O projeto utiliza LocalStack para execução local dos serviços AWS.

O ambiente é iniciado via Docker Compose.

As filas utilizadas pela aplicação já são inicializadas automaticamente durante o ambiente local.

Isso permite:

- ambiente pronto para uso
- testes locais rápidos
- independência da AWS real
- laboratório de mensageria

---

# Fluxo da aplicação

## Inicialização

1. O LocalStack é iniciado
2. As filas são criadas/configuradas
3. A aplicação Spring Boot é iniciada
4. A interface web fica disponível

## Operação

Através da interface é possível:

- selecionar filas
- enviar mensagens
- visualizar mensagens
- acompanhar mensagens existentes
- limpar filas
- inspecionar payloads

---

# Tecnologias utilizadas

| Tecnologia | Descrição |
|---|---|
| Java 21 | Linguagem principal |
| Spring Boot 4 | Base da aplicação |
| JoinFaces | Integração Spring Boot + JSF |
| PrimeFaces | Componentes visuais |
| AWS SDK v2 | Integração com SQS |
| LocalStack | Simulação local AWS |
| Docker | Execução do ambiente |

---

# Como executar

## Subir o ambiente

```bash
docker compose up -d
```

---

## Executar aplicação

```bash
./mvnw spring-boot:run
```

---

## Acessar aplicação

```text
http://localhost:8080
```

---

# Objetivos do projeto

O projeto foi criado com foco em:

- estudos de AWS SQS
- laboratório local
- testes de mensageria
- troubleshooting
- administração visual de filas
- demonstrações técnicas
- experimentação com JSF + AWS SDK

---

# Características do projeto

- interface web administrativa
- integração completa com SQS
- operações visuais
- ambiente local automatizado
- integração com LocalStack
- uso de JSF + PrimeFaces
- arquitetura simples e direta

---

# Licença

Projeto para fins educacionais e laboratoriais.
