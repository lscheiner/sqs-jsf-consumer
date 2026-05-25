# AWS Console

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

