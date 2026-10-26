# tech-challenge-os-service

Aplicação principal do **Tech Challenge — Fase 3**.

Implementa as regras de negócio do sistema de gestão de uma oficina mecânica em **Spring Boot (Java 21)**, executada como workload containerizado no **Amazon EKS**, com autenticação stateless via JWT e banco de dados relacional gerenciado no Amazon RDS PostgreSQL.

---

## Tecnologias

- Java 21 (Amazon Corretto)
- Spring Boot 4.0.5 (Spring MVC, Spring Data JPA, Spring Security, Spring Actuator)
- PostgreSQL 16 / Flyway (13 migrations, schema `oficina`)
- JWT — JJWT (`io.jsonwebtoken:jjwt`), assinatura HMAC-SHA256
- SpringDoc OpenAPI 3 (Swagger UI)
- Docker — multi-stage build (`linux/amd64`)
- Kubernetes 1.36 / Amazon EKS
- GitHub Actions (CI/CD)

---

## Estrutura do Repositório

```
.github/workflows/
├── pr.yml          CI: testes unitários, cobertura JaCoCo e build check da imagem Docker
└── deploy.yml      CD: build, push ECR e deploy no EKS via kubectl
app/
├── src/main/java/com/fiap/techchallenge/
│   ├── cliente/           domínio e endpoints de cliente
│   ├── veiculo/           domínio e endpoints de veículo
│   ├── ordemservico/      domínio e endpoints de ordem de serviço
│   ├── peca/              domínio e endpoints de peça
│   └── security/          filtro JWT, configuração Spring Security
├── src/main/resources/
│   ├── application.properties    configuração e variáveis de ambiente
│   ├── logback-spring.xml        logs JSON em produção, colorido em dev
│   └── db/migration/             migrations Flyway (V1 a V13)
├── src/test/                     testes unitários e de integração (Testcontainers)
├── build.gradle                  dependências, JaCoCo (mínimo 80%) e SonarQube
└── Dockerfile                    multi-stage build para linux/amd64
k8s/
├── configmap.yaml                variáveis não-sensíveis de configuração
├── deployment.yaml               Deployment (2 réplicas, RollingUpdate, probes, non-root)
├── service.yaml                  Service LoadBalancer — AWS NLB, porta 80 → 8080
├── hpa.yaml                      HPA: 2 a 10 réplicas (CPU 70%, memória 80%)
└── pod-disruption-budget.yaml    PDB: minAvailable 1
postman/
├── Tech Challenge Fase 3 - Clientes.postman_collection.json
├── Tech Challenge Fase 3 - Veiculo.postman_collection.json
├── Tech Challenge Fase 3 - Peca e TipoPeca.postman_collection.json
├── Tech Challenge Fase 3 - Servico e TipoServico.postman_collection.json
├── Tech Challenge Fase 3 - Ordem de Servico.postman_collection.json
└── TechChallenge-Fase3.postman_environment.json
docker-compose.yml                stack local: App + PostgreSQL 16 + SonarQube
```

---

## Arquitetura

```
                            ┌───────────────────────────────┐
                            │       Amazon API Gateway      │
                            └───────────────┬───────────────┘
                                            │
                       ┌────────────────────┴─────────────────────┐
                       │ POST /auth                      /{proxy+}│
                       ▼                                          ▼
         ┌──────────────────────────┐          ┌──────────────────────────────┐
         │   AWS Lambda (Auth CPF)  │          │  AWS Network Load Balancer   │
         │   - Valida CPF           │          │  - porta 80                  │
         │   - Emite JWT            │          └──────────────┬───────────────┘
         └──────────────────────────┘                         │
                                                              ▼
                                          ┌───────────────────────────────────┐
                                          │   Amazon EKS (ns: tech-challenge) │
                                          │                                   │
                                          │   Pods Spring Boot                │
                                          │   - Valida JWT                    │
                                          │   - Regras de negócio             │
                                          │   - Actuator / Prometheus         │
                                          │   - Migrations Flyway             │
                                          │                                   │
                                          │   HPA (CPU 70% / Memória 80%)     │
                                          └──────────────────┬────────────────┘
                                                             │
                                          ┌──────────────────▼────────────────┐
                                          │  Amazon RDS PostgreSQL 16         │
                                          │  database: techchallenge          │
                                          │  schema:   oficina                │
                                          └───────────────────────────────────┘
```

---

## Pré-requisitos

- Java JDK 21
- Docker
- kubectl compatível com K8s 1.36 — necessário apenas para deploy manual
- AWS CLI v2 — necessário apenas para deploy manual

Para desenvolvimento local, somente Docker é necessário.

---

## Execução Local

### Docker Compose

Sobe o PostgreSQL 16 e a aplicação compilada via `Dockerfile`:

```bash
docker compose up --build -d
```

A API fica disponível em `http://localhost:8080`.

Para incluir o SonarQube na stack (porta 9000):

```bash
docker compose --profile tools up -d
```

### Gradle

Sobe apenas o banco e executa a aplicação com hot-reload:

```bash
docker compose up -d db
./gradlew bootRun
```

---

## Testes e Cobertura

```bash
./gradlew test jacocoTestReport jacocoTestCoverageVerification
```

Relatório HTML gerado em `app/build/reports/jacoco/test/html/index.html`. A build falha se a cobertura de linhas ficar abaixo de 80%.

---

## Build da Imagem Docker

```bash
docker build --platform linux/amd64 -t tech-challenge-app:local -f app/Dockerfile .
```

O flag `--platform linux/amd64` é obrigatório. Os nós do cluster EKS utilizam `AL2023_x86_64_STANDARD`; uma imagem ARM64 causará `ImagePullBackOff` no deploy.

---

## Variáveis de Ambiente

| Variável | Padrão (dev local) | Descrição |
|---|---|---|
| `SPRING_DATASOURCE_URL` / `DB_URL` | `jdbc:postgresql://localhost:5432/techchallenge` | URL JDBC de conexão com o banco |
| `SPRING_DATASOURCE_USERNAME` / `DB_USER` | `postgres` | Usuário do banco |
| `SPRING_DATASOURCE_PASSWORD` / `DB_PASSWORD` | `admin` | Senha do banco |
| `JWT_SECRET` | `minha-chave-secreta-dev-minimo-32-chars!!` | Chave de assinatura dos tokens JWT (mín. 32 caracteres) |
| `JWT_ACCESS_EXP` | `3600` | Validade do token em segundos |
| `WEBHOOK_APROVACAO_SECRET` | `webhook-secret-key-123` | Chave de validação dos webhooks de transição de OS |

Em produção, essas variáveis são injetadas pelo Kubernetes Secret `app-secrets`, criado automaticamente pela pipeline `deploy.yml`.

---

## CI/CD (GitHub Actions)

| Workflow | Disparo | O que faz |
|---|---|---|
| `pr.yml` | Pull Request para `main` | Compila, executa testes, verifica cobertura JaCoCo e valida o build da imagem Docker |
| `deploy.yml` | Push na `main` ou disparo manual | Autentica na AWS, publica a imagem no ECR, configura o kubectl e aplica os manifestos no EKS |

### Secrets do repositório

| Secret | Descrição |
|---|---|
| `AWS_ACCESS_KEY_ID` | Credencial AWS |
| `AWS_SECRET_ACCESS_KEY` | Credencial AWS |
| `AWS_SESSION_TOKEN` | Token de sessão temporário (AWS Academy Learner Lab) |
| `DB_URL` | String JDBC do RDS (`jdbc:postgresql://<host>:5432/techchallenge`) |
| `DB_USER` | Usuário master do banco |
| `DB_PASSWORD` | Senha do banco |
| `JWT_SECRET` | Chave de assinatura dos tokens JWT (mínimo 32 caracteres) |
| `WEBHOOK_APROVACAO_SECRET` | Chave de validação dos webhooks de transição de ordem de serviço |

---

## Documentação da API

Com a aplicação em execução:

- Swagger UI: `http://<HOST>/swagger-ui/index.html`
- OpenAPI JSON: `http://<HOST>/v3/api-docs`
- Health: `http://<HOST>/actuator/health`
- Métricas Prometheus: `http://<HOST>/actuator/prometheus`
- **Postman Collections:** Coleções completas e arquivo de environment disponíveis na pasta `postman/` (com suporte à autenticação serverless via CPF e chave `baseUrl` centralizada).

---

## Exemplos de Uso da API

As requisições abaixo demonstram o fluxo padrão de operação do sistema. Todas as rotas de negócio requerem o token JWT no cabeçalho `Authorization: Bearer <TOKEN>`, obtido via autenticação de cliente no módulo serverless (`POST /auth` com `{"cpf": "..."}`) ou usuário interno (`POST /api/auth/login`).

### 1. Cadastro e Consulta de Cliente

Cadastra um novo cliente na oficina e realiza a busca por documento.

* **Requisição de Criação (`POST /api/cliente`):**
  ```http
  POST /api/cliente
  Content-Type: application/json
  Authorization: Bearer <TOKEN>

  {
    "nome": "Carlos Silva",
    "tipoPessoa": "FISICA",
    "documento": "52998224725",
    "dataNascimento": "1990-01-15",
    "email": "carlos.silva@exemplo.com"
  }
  ```

* **Resposta (`201 Created`):**
  ```http
  Location: /api/cliente/1
  ```

* **Consulta por Documento (`GET /api/cliente?documento=52998224725`):**
  ```json
  [
    {
      "id": 1,
      "nome": "Carlos Silva",
      "tipoPessoa": "FISICA",
      "documento": "52998224725",
      "dataNascimento": "1990-01-15",
      "email": "carlos.silva@exemplo.com"
    }
  ]
  ```

---

### 2. Abertura de Ordem de Serviço

Cria uma nova ordem de serviço vinculada ao cliente e ao veículo.

* **Requisição (`POST /api/ordemDeServico`):**
  ```http
  POST /api/ordemDeServico
  Content-Type: application/json
  Authorization: Bearer <TOKEN>

  {
    "solicitacao": "Troca de pastilhas de freio dianteiras e alinhamento",
    "idCliente": 1,
    "idVeiculo": 1,
    "pecas": [],
    "servicos": []
  }
  ```

* **Resposta (`201 Created`):**
  ```json
  {
    "id": 1,
    "solicitacao": "Troca de pastilhas de freio dianteiras e alinhamento",
    "status": "RECEBIDA",
    "orcamento": 0.0,
    "cliente": {
      "id": 1,
      "nome": "Carlos Silva",
      "documento": "52998224725"
    },
    "veiculo": {
      "id": 1,
      "placa": "ABC1D23",
      "modelo": "Civic"
    },
    "itensPeca": [],
    "itensServico": []
  }
  ```

---

### 3. Consulta de Ordens de Serviço por Cliente

Lista o histórico e o status de todas as ordens de serviço de um cliente específico.

* **Requisição (`GET /api/ordemDeServico/cliente/1/ordens`):**
  ```http
  GET /api/ordemDeServico/cliente/1/ordens
  Authorization: Bearer <TOKEN>
  ```

* **Resposta (`200 OK`):**
  ```json
  [
    {
      "id": 1,
      "solicitacao": "Troca de pastilhas de freio dianteiras e alinhamento",
      "status": "RECEBIDA",
      "orcamento": 0.0,
      "cliente": {
        "id": 1,
        "nome": "Carlos Silva"
      },
      "veiculo": {
        "id": 1,
        "placa": "ABC1D23"
      }
    }
  ]
  ```

---

## Análise de Código e Segurança

### SonarQube

```bash
docker compose --profile tools up -d sonarqube
# Acesse http://localhost:9000, gere um token e execute:
./gradlew test jacocoTestReport sonar -Dsonar.token=<TOKEN>
```

### OWASP ZAP

```bash
# Obter token JWT
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"login":"gerente","senha":"senha@123"}' | jq -r .accessToken)

# Executar scan na especificação OpenAPI
mkdir -p reports
docker run --rm -v "$(pwd)/reports:/zap/wrk/:rw" zaproxy/zap-stable zap-api-scan.py \
  -t http://host.docker.internal:8080/v3/api-docs \
  -f openapi \
  -r zap-report.html \
  -z "-config replacer.full_list(0).matchtype=REQ_HEADER \
      -config replacer.full_list(0).matchstr=Authorization \
      -config replacer.full_list(0).replacement=\"Bearer $TOKEN\""
```

O relatório é gerado em `reports/zap-report.html`.
