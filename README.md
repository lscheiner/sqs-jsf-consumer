# SQS JSF Consumer - Documentação

## 📋 Visão Geral

**SQS JSF Consumer** é uma aplicação web construída com **Spring Boot**, **Jakarta Faces (JSF)** e **PrimeFaces** que fornece uma interface gráfica para consumir e produzir mensagens em filas **AWS SQS**. O projeto utiliza **LocalStack** para simular o ambiente AWS localmente.

### Características Principais
- ✅ Interface web intuitiva com PrimeFaces
- ✅ Consumo de mensagens de filas SQS
- ✅ Produção de mensagens em filas SQS
- ✅ Suporte a LocalStack para desenvolvimento local
- ✅ Configuração de múltiplas filas
- ✅ Visualização de conteúdo de mensagens

---

## 🛠️ Stack Tecnológico

| Tecnologia | Versão | Percentual |
|-----------|--------|-----------|
| **Java** | 21 | 50.2% |
| **HTML** | - | 42.9% |
| **Shell** | - | 6.9% |

### Dependências Principais
- **Spring Boot** 4.0.6
- **JoinFaces** 6.0.6.1 (Spring Boot para JSF)
- **PrimeFaces** (via JoinFaces)
- **AWS SDK SQS** 2.44.3
- **Jakarta Servlet** e **Jakarta Faces**

---

## 📁 Estrutura do Projeto

```
sqs-jsf-consumer/
├── src/main/java/br/com/scheiner/sqs/console/
│   ├── SqsConsumerApplication.java          # Classe principal
│   ├── config/
│   │   └── SqsConfig.java                  # Configuração do cliente SQS
│   ├── consumer/
│   │   └── SqsConsumerService.java         # Serviço de consumo de mensagens
│   ├── producer/
│   │   └── SqsProducerService.java         # Serviço de produção de mensagens
│   ├── controller/
│   │   ├── SqsConsumerController.java      # Controller do consumidor (JSF)
│   │   └── SqsProducerController.java      # Controller do produtor (JSF)
│   └── redirect/
│       └── RedirectServlet.java            # Servlet para redirecionamento
├── src/main/webapp/
│   ├── index.xhtml                         # Página principal
│   └── template/                           # Templates Facelets
├── src/main/resources/
│   ├── application.properties               # Configurações da aplicação
│   └── META-INF/
├── pom.xml                                  # Dependências Maven
├── mvnw / mvnw.cmd                         # Maven Wrapper
└── localstack/                             # Configuração LocalStack
```

---

## 🔧 Configuração

### Arquivo: `application.properties`

```properties
spring.application.name=sqs-consumer
joinfaces.faces.project-stage=Production
joinfaces.primefaces.theme=saga
joinfaces.primefaces.font-awesome=true
joinfaces.faces-servlet.url-mappings=*.xhtml
joinfaces.faces-servlet.enabled=true

aws.region=us-east-1
aws.sqs.endpoint=http://localhost:4566

app.sqs.filas=pedido-criado,pedido-cancelado,cliente-atualizado,envio-email,geracao-boleto
```

**Filas Configuradas:**
- `pedido-criado`
- `pedido-cancelado`
- `cliente-atualizado`
- `envio-email`
- `geracao-boleto`

---

## 🏗️ Componentes Principais

### 1. **SqsConfig** - Configuração do Cliente SQS
```java
// Configura o cliente SQS com credenciais LocalStack
// Endpoint: http://localhost:4566
// Região: us-east-1
// Credenciais padrão: test/test
```

### 2. **SqsConsumerService** - Serviço de Consumo
```java
consumirMensagens(String fila, Integer quantidadeMensagens)
// Consome mensagens de uma fila específica
// Parâmetros:
//   - fila: nome da fila
//   - quantidadeMensagens: quantidade a consumir (padrão: 5)
// Retorna: List<Message>
```

### 3. **SqsProducerService** - Serviço de Produção
```java
enviarMensagem(String fila, String payload)
// Envia mensagem para fila
// Inclui atributo: content-type = application/json
```

### 4. **SqsConsumerController** - Controller JSF do Consumidor
- Seleciona fila
- Define quantidade de mensagens a buscar
- Visualiza conteúdo de mensagens
- Gerencia estado da tela de consumo

### 5. **SqsProducerController** - Controller JSF do Produtor
- Seleciona fila
- Define payload da mensagem
- Envia mensagem com notificação de sucesso
- Loga operações

### 6. **RedirectServlet** - Redirecionamento
- Redireciona requisições `/` para `/index.xhtml`

---

## 🚀 Como Executar

### Pré-requisitos
- Java 21+
- Maven 3.8.1+
- Docker (para LocalStack)

### 1. Iniciar LocalStack
```bash
docker run -d -p 4566:4566 localstack/localstack
```

### 2. Criar Filas SQS no LocalStack
```bash
aws --endpoint-url=http://localhost:4566 sqs create-queue \
  --queue-name pedido-criado \
  --region us-east-1
```

### 3. Compilar e Executar
```bash
# Usar Maven Wrapper
./mvnw spring-boot:run

# Ou
mvn spring-boot:run
```

### 4. Acessar a Aplicação
```
http://localhost:8080
```

---

## 📊 Fluxo da Aplicação

```
┌─────────────────────────────────────────────┐
│         Interface Web (JSF/PrimeFaces)      │
├──────────────────┬──────────────────────────┤
│  Consumidor      │      Produtor            │
├──────────────────┼──────────────────────────┤
│ SqsConsumerCtrl  │  SqsProducerCtrl         │
│        ↓         │           ↓              │
│ SqsConsumerSvc   │  SqsProducerSvc          │
└────────┬─────────┴─────────┬────────────────┘
         │                   │
         └───────┬───────────┘
                 ↓
         ┌──────────────────┐
         │   SqsConfig      │
         │  (SqsClient)     │
         └────────┬─────────┘
                  ↓
         ┌──────────────────┐
         │  LocalStack SQS  │
         │ localhost:4566   │
         └──────────────────┘
```

---

## 📝 Funcionalidades

### Consumidor
1. Selecionar fila na dropdown
2. Definir quantidade de mensagens a consumir
3. Clicar em "Buscar Mensagens"
4. Visualizar mensagens em tabela
5. Clicar em mensagem para ver conteúdo

### Produtor
1. Selecionar fila na dropdown
2. Inserir payload (JSON ou texto)
3. Clicar em "Enviar Mensagem"
4. Visualizar notificação de sucesso
5. Opção de "Limpar" formulário

---

## 🔐 Segurança

⚠️ **Nota:** As credenciais AWS são hardcoded no `SqsConfig.java` (test/test) apenas para fins de desenvolvimento com LocalStack. **Nunca use em produção!**

Para produção, considere:
- Usar AWS Secrets Manager
- Implementar autenticação IAM
- Usar variáveis de ambiente
- Implementar Spring Security

---

## 📦 Construir JAR

```bash
./mvnw clean package
```

O arquivo WAR será gerado em: `target/sqs-console-0.0.1-SNAPSHOT.war`

---

## 📚 Recursos Adicionais

- [AWS SDK para Java](https://docs.aws.amazon.com/sdk-for-java/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Jakarta Faces (JSF)](https://jakarta.ee/specifications/faces/)
- [PrimeFaces](https://www.primefaces.org/)
- [LocalStack](https://localstack.cloud/)

---

## 👤 Autor

**lscheiner** - [GitHub Profile](https://github.com/lscheiner)

---

## 📄 Licença

Sem licença especificada no projeto.

---

**Última atualização:** 2026-05-09
