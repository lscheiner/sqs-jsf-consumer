# AWS Console

<p align="center">

<img src="https://github.com/lscheiner/sqs-jsf-consumer/actions/workflows/pipeline.yml/badge.svg" alt="Pipeline Java"/>

<a href="https://sonarcloud.io/summary/new_code?id=lscheiner_sqs-jsf-consumer">
  <img src="https://sonarcloud.io/api/project_badges/measure?project=lscheiner_sqs-jsf-consumer&metric=alert_status" alt="Quality Gate"/>
</a>

<a href="https://sonarcloud.io/summary/new_code?id=lscheiner_sqs-jsf-consumer">
  <img src="https://sonarcloud.io/api/project_badges/measure?project=lscheiner_sqs-jsf-consumer&metric=coverage" alt="Coverage"/>
</a>

<a href="https://sonarcloud.io/summary/new_code?id=lscheiner_sqs-jsf-consumer">
  <img src="https://sonarcloud.io/api/project_badges/measure?project=lscheiner_sqs-jsf-consumer&metric=bugs" alt="Bugs"/>
</a>

<a href="https://sonarcloud.io/summary/new_code?id=lscheiner_sqs-jsf-consumer">
  <img src="https://sonarcloud.io/api/project_badges/measure?project=lscheiner_sqs-jsf-consumer&metric=vulnerabilities" alt="Vulnerabilities"/>
</a>

</p>

<p align="center">

<a href="https://sonarcloud.io/summary/new_code?id=lscheiner_sqs-jsf-consumer">
  <img src="https://sonarcloud.io/api/project_badges/measure?project=lscheiner_sqs-jsf-consumer&metric=code_smells" alt="Code Smells"/>
</a>

<a href="https://sonarcloud.io/summary/new_code?id=lscheiner_sqs-jsf-consumer">
  <img src="https://sonarcloud.io/api/project_badges/measure?project=lscheiner_sqs-jsf-consumer&metric=duplicated_lines_density" alt="Duplicated Lines"/>
</a>

<a href="https://sonarcloud.io/summary/new_code?id=lscheiner_sqs-jsf-consumer">
  <img src="https://sonarcloud.io/api/project_badges/measure?project=lscheiner_sqs-jsf-consumer&metric=ncloc" alt="Lines of Code"/>
</a>

<a href="https://sonarcloud.io/summary/new_code?id=lscheiner_sqs-jsf-consumer">
  <img src="https://sonarcloud.io/api/project_badges/measure?project=lscheiner_sqs-jsf-consumer&metric=reliability_rating" alt="Reliability"/>
</a>

<a href="https://sonarcloud.io/summary/new_code?id=lscheiner_sqs-jsf-consumer">
  <img src="https://sonarcloud.io/api/project_badges/measure?project=lscheiner_sqs-jsf-consumer&metric=security_rating" alt="Security"/>
</a>

<a href="https://sonarcloud.io/summary/new_code?id=lscheiner_sqs-jsf-consumer">
  <img src="https://sonarcloud.io/api/project_badges/measure?project=lscheiner_sqs-jsf-consumer&metric=sqale_rating" alt="Maintainability"/>
</a>

</p>

<p align="center">

<img src="https://img.shields.io/badge/Java-21-orange?logo=openjdk" alt="Java 21"/>
<img src="https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?logo=springboot" alt="Spring Boot"/>
<img src="https://img.shields.io/badge/JSF-PrimeFaces-blue" alt="JSF"/>
<img src="https://img.shields.io/badge/AWS-SDK_v2-FF9900?logo=amazonaws" alt="AWS SDK"/>
<img src="https://img.shields.io/badge/Redis-Jedis-red?logo=redis" alt="Redis"/>
<img src="https://img.shields.io/badge/Docker-enabled-2496ED?logo=docker" alt="Docker"/>

</p>

---

Aplicação Java desenvolvida com Spring Boot + JSF + PrimeFaces para gerenciamento de recursos AWS/local.

O projeto possui interface web para administração de:

- SQS
- DynamoDB
- Redis
- configurações dinâmicas de conexão

---

# Tecnologias

- Java 21
- Spring Boot
- JSF
- PrimeFaces
- AWS SDK v2
- Redis (Jedis)
- LocalStack
- Maven
- Docker

---

# SQS

Módulo para gerenciamento de filas SQS.

## Recursos

- listar filas
- consumir mensagens
- enviar mensagens
- purge de filas
- configuração dinâmica

## Componentes principais

### SqsClientGateway

Interface responsável pelo acesso ao SQS.

### DefaultSqsClientGateway

Implementação responsável por:

- gerenciamento do `SqsClient`
- integração AWS SDK
- troca dinâmica do client
- lifecycle do client

A reconfiguração utiliza:

```java
AtomicReference<SqsClient>
```

### SqsQueueUrlResolver

Responsável por:

- montar queue URLs
- extrair nome da fila

## Services

- `SqsConsumerService`
- `SqsProducerService`
- `SqsQueueService`

## Controllers

- `SqsConsumerController`
- `SqsProducerController`
- `SqsAdminController`

---

# DynamoDB

Módulo para gerenciamento de tabelas DynamoDB.

## Recursos

- listar tabelas
- visualizar metadados
- integração com LocalStack

## Componentes principais

### DynamoDbClientGateway

Contrato de acesso ao DynamoDB.

### DefaultDynamoDbClientGateway

Implementação padrão do gateway.

### DynamodbService

Serviço responsável pelas operações.

### DynamoDbController

Controller JSF do módulo.

---

# Redis

Módulo para gerenciamento de registros Redis.

## Recursos

- visualizar registros
- incluir registros
- editar registros
- excluir registros
- configuração dinâmica

## Componentes principais

### RedisClientProvider

Gerenciamento da conexão Redis.

### RedisService

Operações Redis.

### RedisController

Controller JSF responsável pela interface.

## Modelos

- `RedisConfiguracao`
- `RedisRegistro`

---

# Health Check

Serviços responsáveis pela validação de conectividade.

## Implementações

- `SqsHealthService`
- `DynamoDbHealthService`

---

# Configuração

A aplicação permite alteração dinâmica de configurações sem reinicialização.

## Configurações suportadas

- endpoint
- região AWS
- access key
- secret key
- TLS
- configuração Redis

## Componentes

### AwsConfiguration

Configuração principal da aplicação.

### RedisConnectionConfiguration

Configuração Redis.

### ConfigController

Tela/configuração dinâmica.

---

# Estrutura do Projeto

```text
src/main/java/br/com/scheiner/aws/console
├── config
├── controller
├── dynamodb
├── health
├── model
├── redirect
├── service
├── sqs
└── utils
```

---

# Execução

## Build

```bash
mvn clean install
```

## Executar aplicação

```bash
mvn spring-boot:run
```

---

