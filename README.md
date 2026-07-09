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
<img src="https://img.shields.io/badge/Spring_Boot-4.0.6-6DB33F?logo=springboot" alt="Spring Boot"/>
<img src="https://img.shields.io/badge/JoinFaces-6.0.6.1-blue" alt="JoinFaces"/>
<img src="https://img.shields.io/badge/PrimeFaces-JSF-blue" alt="PrimeFaces"/>
<img src="https://img.shields.io/badge/AWS_SDK-v2-FF9900?logo=amazonaws" alt="AWS SDK"/>
<img src="https://img.shields.io/badge/LocalStack-4.13.0-purple" alt="LocalStack"/>
<img src="https://img.shields.io/badge/Redis-Lettuce-red?logo=redis" alt="Redis"/>
<img src="https://img.shields.io/badge/Maven-WAR-C71A36?logo=apachemaven" alt="Maven"/>
<img src="https://img.shields.io/badge/Docker-enabled-2496ED?logo=docker" alt="Docker"/>

</p>

Aplicação web Java para explorar e administrar recursos AWS em ambiente local, principalmente via LocalStack. A interface é construída com JSF/PrimeFaces e roda sobre Spring Boot, oferecendo telas para SQS, SNS, DynamoDB, Redis, dashboard de recursos e configuração de endpoint/região.

O projeto é útil para desenvolvimento e testes locais de integrações assíncronas, permitindo publicar mensagens, inspecionar filas, reenviar mensagens de DLQ, editar itens em tabelas DynamoDB e manipular chaves Redis sem depender do console oficial da AWS.

## Sumário

- [Principais recursos](#principais-recursos)
- [Tecnologias](#tecnologias)
- [Arquitetura](#arquitetura)
- [Módulos da aplicação](#módulos-da-aplicação)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Configuração](#configuração)
- [Executando localmente](#executando-localmente)
- [LocalStack](#localstack)
- [Rotas da interface](#rotas-da-interface)
- [Build, testes e qualidade](#build-testes-e-qualidade)

## Principais recursos

- Dashboard consolidado com status e contagem de recursos SQS, SNS, DynamoDB e Redis.
- Explorer SQS com listagem de filas, detalhes, leitura de mensagens, envio, replay, exclusão e purge.
- Apoio a DLQ: identificação de filas mortas, abertura da DLQ e reenvio para a fila original quando a relação é conhecida.
- Administração SNS com listagem de tópicos, assinaturas e publicação de mensagens JSON.
- Administração DynamoDB com listagem de tabelas, metadados, scan de itens, criação, edição e exclusão via JSON no formato de `AttributeValue`.
- Administração Redis com listagem de chaves, edição de valor, TTL, exclusão e configuração dinâmica de conexão.
- Configuração dinâmica de endpoint e região AWS sem reiniciar a aplicação.
- Bootstrap local com Docker Compose, LocalStack e Redis.

## Tecnologias

| Tecnologia | Uso no projeto |
| --- | --- |
| Java 21 | Linguagem principal da aplicação. |
| Spring Boot 4.0.6 | Bootstrap, injeção de dependências, configuração e empacotamento. |
| JoinFaces 6.0.6.1 | Integração entre Spring Boot, JSF e PrimeFaces. |
| JSF / Jakarta Faces | Modelo de telas server-side com beans `@Named` e `@ViewScoped`. |
| PrimeFaces | Componentes visuais, grids, dialogs, growls, árvore de recursos e Ajax. |
| AWS SDK for Java v2 | Clientes SQS, SNS e DynamoDB. |
| LocalStack 4.13.0 | Emulação local de SQS, SNS e DynamoDB. |
| Redis | Banco chave-valor local para o módulo Redis. |
| Lettuce | Cliente Redis usado pelo Spring Data Redis. |
| Jackson | Formatação, validação e conversão de JSON. |
| Commons Validator | Validação de URL na tela de configuração. |
| Maven | Build, testes e empacotamento `war`. |
| Docker Compose | Subida de LocalStack e Redis para desenvolvimento local. |
| GitHub Actions / SonarCloud | Pipeline, testes, build e análise de qualidade. |

## Arquitetura

A aplicação segue uma arquitetura em camadas simples, organizada por domínio:

```text
Tela JSF (.xhtml)
        |
Controller JSF (@Named, @ViewScoped)
        |
Service de domínio
        |
Gateway / Provider de client
        |
AWS SDK v2, Redis ou configuração local
```

### Fluxo geral

1. As páginas `.xhtml` declaram a interface e acionam métodos nos controllers JSF.
2. Os controllers controlam estado de tela, validações de entrada, mensagens para o usuário e navegação.
3. Os services concentram as regras de uso de cada recurso, como listar filas, converter JSON, publicar mensagem ou salvar item.
4. Os gateways encapsulam clients externos, como `SqsClient`, `SnsClient` e `DynamoDbClient`.
5. As configurações dinâmicas ficam em componentes com `AtomicReference`, permitindo trocar endpoint, região ou conexão Redis em runtime.
6. O dashboard consulta implementações de `ResourceInfoProvider` para montar uma visão agregada e navegável dos recursos.

### Padrões usados

- **Controller por tela**: cada página principal possui um bean JSF próprio.
- **Service por domínio**: a regra de negócio fica fora do controller.
- **Gateway para AWS SDK**: os clients AWS ficam isolados e podem ser recriados quando a configuração muda.
- **Provider de recursos**: o dashboard não conhece os detalhes de cada serviço; ele consome contratos comuns de resumo.
- **Navegação centralizada**: `ApplicationRoute` e `NavigationManager` cuidam das rotas e parâmetros de recurso.
- **JSON como formato operacional**: mensagens SQS/SNS e itens DynamoDB são manipulados como JSON para facilitar testes manuais.

## Módulos da aplicação

### Dashboard

Pacote base: `br.com.scheiner.aws.console.dashboard`

O dashboard agrega os recursos disponíveis em SQS, SNS, DynamoDB e Redis. Ele exibe indicadores de status, contagens e uma árvore navegável de recursos.

Componentes principais:

- `DashboardController`: controller JSF da tela inicial.
- `DashboardService`: consulta todos os `ResourceInfoProvider` registrados e monta `DashboardData`.
- `DashboardSummary`: resumo com contagens, endpoint LocalStack, host Redis e status.
- `ResourceNode`: nó usado na árvore de recursos da interface.

Ao selecionar um recurso na árvore, o dashboard redireciona para a tela específica já tentando selecionar o recurso pelo parâmetro da URL.

### SQS

Pacote base: `br.com.scheiner.aws.console.sqs`

Módulo de exploração e operação de filas SQS.

Recursos:

- Listar filas ordenadas por nome.
- Identificar filas Standard, FIFO e DLQs.
- Exibir atributos principais da fila, como ARN, visibility timeout, retenção, wait time e redrive policy.
- Buscar mensagens com `receiveMessage`.
- Enviar mensagens JSON com `MessageAttributes`.
- Aplicar `content-type=application/json` como atributo padrão quando nenhum atributo é informado.
- Visualizar corpo formatado.
- Baixar mensagem em arquivo JSON.
- Reenviar mensagem para a mesma fila.
- Reenviar mensagem de DLQ para a fila original quando a relação é detectada.
- Excluir mensagem pelo `receiptHandle`.
- Executar purge da fila.

Componentes principais:

- `SqsExplorerController`: estado da tela, seleção de fila, ações de envio, replay, delete e purge.
- `SqsExplorerService`: operações SQS, leitura de atributos, conversão de mensagens e validação JSON.
- `SqsClientGateway`: contrato do client SQS.
- `DefaultSqsClientGateway`: cria, expõe e reconfigura o `SqsClient`.
- `SqsQueueUrlResolver`: monta URLs no padrão LocalStack e extrai nomes de fila.
- `SqsHealthService`: valida conectividade do client.
- `SqsResourceInfoProvider`: fornece resumo de filas para o dashboard.

### SNS

Pacote base: `br.com.scheiner.aws.console.sns`

Módulo para explorar tópicos SNS e publicar mensagens.

Recursos:

- Listar tópicos SNS.
- Filtrar tópicos por nome ou ARN.
- Listar assinaturas de um tópico.
- Publicar mensagens JSON.
- Informar `subject` opcional.
- Enviar `MessageAttributes` em JSON.
- Aplicar `content-type=application/json` como atributo padrão.

Componentes principais:

- `SnsController`: controller JSF da tela SNS.
- `SnsService`: listagem de tópicos, assinaturas, publicação e conversão de atributos.
- `SnsClientGateway`: contrato do client SNS.
- `DefaultSnsClientGateway`: cria e reconfigura o `SnsClient`.
- `SnsHealthService`: valida conectividade com SNS.
- `SnsResourceInfoProvider`: fornece resumo de tópicos para o dashboard.

### DynamoDB

Pacote base: `br.com.scheiner.aws.console.dynamodb`

Módulo de inspeção e edição de tabelas DynamoDB.

Recursos:

- Listar tabelas.
- Visualizar descrição da tabela.
- Exibir chave de partição, chave de ordenação, tipos e status.
- Fazer scan dos itens da tabela.
- Montar colunas dinamicamente a partir dos itens encontrados.
- Criar novos itens.
- Editar itens existentes.
- Excluir itens pela chave.
- Visualizar valores complexos em JSON.
- Converter JSON para `AttributeValue` e `AttributeValue` para JSON.

Componentes principais:

- `DynamoDbController`: controller JSF para seleção de tabela, CRUD de itens e visualização.
- `DynamoDbService`: operações DynamoDB via AWS SDK.
- `DynamoDbClientGateway`: contrato do client DynamoDB.
- `DefaultDynamoDbClientGateway`: cria e reconfigura o `DynamoDbClient`.
- `DynamoDbJsonMapper`: conversor entre JSON e `AttributeValue`.
- `DynamoDbTableMetadata`: extrai metadados da tabela para a tela.
- `DynamoDbHealthService`: valida conectividade com DynamoDB.
- `DynamoDbResourceInfoProvider`: fornece resumo de tabelas para o dashboard.

Formato esperado para editar itens:

```json
{
  "orderId": { "S": "123" },
  "orderStatus": { "S": "CREATED" },
  "amount": { "N": "99.90" },
  "active": { "BOOL": true }
}
```

### Redis

Pacote base: `br.com.scheiner.aws.console.redis`

Módulo para administrar registros Redis.

Recursos:

- Listar chaves com valor e TTL.
- Criar registros.
- Editar chave, valor e TTL.
- Excluir registros.
- Exibir TTL como segundos, sem expiração ou expirado.
- Testar conexão.
- Alterar host, porta, TLS, usuário e senha em runtime.

Componentes principais:

- `RedisController`: controller JSF da tela Redis.
- `RedisService`: operações de leitura, escrita, exclusão e configuração.
- `RedisClientProvider`: cria e reaproveita conexão Lettuce.
- `RedisConnectionConfiguration`: guarda configuração Redis em `AtomicReference`.
- `RedisRegistro`: modelo de item exibido na tela.
- `RedisConfiguracao`: modelo da configuração de conexão.
- `RedisResourceInfoProvider`: fornece resumo de chaves para o dashboard.

### Configuração AWS

Pacote base: `br.com.scheiner.aws.console.configuration`

Tela para alterar endpoint e região AWS usados pelos clients SQS, SNS e DynamoDB.

Componentes principais:

- `ConfigController`: controller da tela de configuração.
- `AwsConfiguration`: guarda endpoint e região atuais.
- `AwsProvider`: contrato implementado pelos gateways AWS para reconfiguração e health check.

Quando a configuração é aplicada, todos os providers AWS registrados recriam seus clients e a tela executa um teste de conexão.

### Navegação e recursos compartilhados

Pacotes principais:

- `br.com.scheiner.aws.console.web.navigation`
- `br.com.scheiner.aws.console.resource`
- `br.com.scheiner.aws.console.utils`

Responsabilidades:

- `ApplicationRoute`: centraliza rotas e parâmetros de seleção de recurso.
- `NavigationManager`: lê parâmetros da requisição e executa redirects JSF.
- `ResourceInfoProvider`: contrato comum para alimentar o dashboard.
- `ResourceInfo`, `ResourceDescriptor`, `ResourceType`, `ServiceStatus`: modelos compartilhados de status e descrição de recursos.
- `JsonUtils`: formatação segura de JSON para exibição.

## Estrutura do projeto

```text
.
├── localstack/
│   ├── docker-compose.yml
│   ├── localstack/init/ready.d/
│   │   ├── 01-create-tables.sh
│   │   ├── 04-create-sqs.sh
│   │   └── 05-create-sns.sh
│   └── localstack-config/
│       ├── dynamodb/
│       ├── sns/
│       └── sqs/
├── src/main/java/br/com/scheiner/aws/console/
│   ├── configuration/
│   ├── controller/
│   ├── dashboard/
│   ├── dynamodb/
│   ├── redis/
│   ├── resource/
│   ├── sns/
│   ├── sqs/
│   ├── utils/
│   └── web/navigation/
├── src/main/resources/
│   └── application.properties
├── src/main/webapp/
│   ├── configuracao.xhtml
│   ├── dashboard.xhtml
│   ├── dynamodb.xhtml
│   ├── redis.xhtml
│   ├── sns.xhtml
│   ├── sqs-explorer.xhtml
│   └── template/template.xhtml
├── pom.xml
└── README.md
```

## Configuração

Arquivo principal: `src/main/resources/application.properties`

```properties
server.port=3000
spring.application.name=aws-console

joinfaces.faces.project-stage=Production
joinfaces.primefaces.theme=saga
joinfaces.primefaces.font-awesome=true
joinfaces.faces-servlet.url-mappings=*.xhtml
joinfaces.faces-servlet.enabled=true

aws.region=sa-east-1
aws.endpoint=http://localhost:4566

redis.host=localhost
redis.port=6379
redis.tls=false
redis.username=
redis.password=
```

Configurações importantes:

- `server.port`: porta HTTP da aplicação.
- `aws.endpoint`: endpoint usado pelos clients AWS. Para LocalStack, o padrão é `http://localhost:4566`.
- `aws.region`: região usada pelos clients AWS. O ambiente local usa `sa-east-1`.
- `redis.host`, `redis.port`, `redis.tls`, `redis.username`, `redis.password`: conexão Redis.
- `joinfaces.primefaces.theme`: tema PrimeFaces. O arquivo comenta as opções `saga`, `vela` e `arya`.

## Executando localmente

### Pré-requisitos

- JDK 21.
- Maven 3.9+ ou Maven Wrapper do projeto.
- Docker e Docker Compose.

### Subir infraestrutura local

No diretório do projeto:

```bash
cd localstack
docker compose up -d
```

Isso sobe:

- LocalStack na porta `4566`.
- Redis na porta `6379`.
- Recursos AWS locais criados pelos scripts em `localstack/localstack/init/ready.d`.

### Executar a aplicação

Na raiz do projeto:

```bash
./mvnw spring-boot:run
```

No Windows:

```bash
mvnw.cmd spring-boot:run
```

Acesse:

```text
http://localhost:3000/dashboard.xhtml
```

### Build

```bash
./mvnw clean package
```

No Windows:

```bash
mvnw.cmd clean package
```

O projeto usa empacotamento `war`.

## LocalStack

O ambiente local está em `localstack/docker-compose.yml` e habilita os serviços:

- SQS
- SNS
- DynamoDB

### Recursos criados automaticamente

#### DynamoDB

Tabela criada a partir de `localstack/localstack-config/dynamodb/tabela1.json`:

- `Orders`
  - Partition key: `orderId` (`S`)
  - Sort key: `orderStatus` (`S`)
  - Billing mode: `PAY_PER_REQUEST`
  - Stream: `NEW_AND_OLD_IMAGES`

#### SQS

Filas configuradas em `localstack/localstack-config/sqs`:

- `pedido-criado`
- `pedido-cancelado`
- `geracao-boleto`
- `envio-email`
- `cliente-atualizado`

O script `04-create-sqs.sh` cria DLQs automaticamente para as filas que não definem `CreateDlq: false`. No conjunto atual, `pedido-cancelado` é criada sem DLQ.

#### SNS

Tópico configurado em `localstack/localstack-config/sns/sns-envio-email.json`:

- `sns-envio-email`
  - Assinatura SQS: `envio-email`

O script `05-create-sns.sh` cria o tópico, aplica policy na fila SQS de destino e registra a assinatura SNS -> SQS.

## Rotas da interface

| Rota | Tela |
| --- | --- |
| `/dashboard.xhtml` | Dashboard geral. |
| `/sqs-explorer.xhtml` | Explorer SQS. |
| `/sns.xhtml` | Administração SNS. |
| `/dynamodb.xhtml` | Administração DynamoDB. |
| `/redis.xhtml` | Administração Redis. |
| `/configuracao.xhtml` | Configuração AWS. |

Algumas rotas aceitam parâmetros para seleção automática:

| Rota | Parâmetro | Exemplo |
| --- | --- | --- |
| `/sqs-explorer.xhtml` | `fila` | `/sqs-explorer.xhtml?fila=envio-email` |
| `/sns.xhtml` | `topico` | `/sns.xhtml?topico=sns-envio-email` |
| `/dynamodb.xhtml` | `tabela` | `/dynamodb.xhtml?tabela=Orders` |

## Build, testes e qualidade

### Testes

```bash
./mvnw test
```

No Windows:

```bash
mvnw.cmd test
```

### Pipeline

O workflow `.github/workflows/pipeline.yml` executa em pushes para `main`:

1. `mvn clean test`
2. análise SonarCloud com Maven
3. `mvn clean package -DskipTests`

### SonarCloud

Os badges no topo do README apontam para o projeto `lscheiner_sqs-jsf-consumer` no SonarCloud e mostram indicadores de qualidade, cobertura, bugs, vulnerabilidades, code smells, duplicação, linhas de código, confiabilidade, segurança e manutenibilidade.

## Observações de desenvolvimento

- Os clients AWS usam credenciais locais fixas `test/test`, padrão comum em LocalStack.
- A troca de endpoint/região recria os clients AWS existentes e fecha os clients antigos.
- O explorer SQS usa `visibilityTimeout` curto ao buscar mensagens para evitar prender mensagens por muito tempo durante inspeções.
- O módulo DynamoDB trabalha com o formato explícito de tipos do AWS SDK, como `{ "S": "texto" }`, `{ "N": "10" }`, `{ "BOOL": true }`, `{ "M": { ... } }` e `{ "L": [ ... ] }`.
- O Redis usa `KEYS *`, adequado para ambiente local e exploração manual, mas não indicado para bases grandes em produção.
- A aplicação é voltada a ambiente local/desenvolvimento; antes de apontar para AWS real, revise credenciais, permissões, endpoints e operações destrutivas como purge/delete.
