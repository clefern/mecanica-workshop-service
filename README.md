# mecanica-workshop-service

> Gerencia a execução física do reparo pelo mecânico, persistindo o histórico em MongoDB (Fase 4, Grupo 14SOAT).

## Responsabilidade na Saga

Este serviço é o **último participante** da Saga no fluxo feliz. Ao receber `IniciarExecucaoCommand`, localiza um mecânico disponível, registra a execução no MongoDB e publica `ExecucaoFinalizadaEvent` para que o orquestrador marque a OS como `ENTREGUE`. Caso não haja mecânico disponível ou ocorra qualquer erro, publica `FalhaNaExecucaoEvent`.

```
os-service → [IniciarExecucaoCommand] → workshop-service
workshop-service → [ExecucaoFinalizadaEvent | FalhaNaExecucaoEvent] → os-service
```

## Endpoints REST

| Método | Path | Descrição |
|--------|------|-----------|
| `GET` | `/api/execucoes/{id}` | Buscar execução por ID |
| `GET` | `/api/execucoes/os/{osId}` | Buscar execução pela OS vinculada |

Swagger: `http://localhost:8083/swagger-ui.html`

## Mensagens RabbitMQ

### Consome
| Queue | Tipo | Ação |
|-------|------|------|
| `mecanica.workshop.iniciar-execucao` | `IniciarExecucaoCommand` | Executa reparo e registra no MongoDB |

### Publica
| Routing Key | Tipo | Condição |
|-------------|------|----------|
| `os.execucao-finalizada` | `ExecucaoFinalizadaEvent` | Reparo concluído com sucesso |
| `os.falha-execucao` | `FalhaNaExecucaoEvent` | Sem mecânico disponível ou erro interno |

Idempotência garantida por `processed_commands` collection (deduplicação por `sagaId`).

## Mecânicos (seed)

| ID | Nome | Especialidade |
|----|------|---------------|
| `a1b2c3d4-0001-…` | Carlos Silva | Motor |
| `a1b2c3d4-0002-…` | Ana Souza | Suspensão |

## Como rodar localmente

```bash
# Stack completa (todos os MS + infra)
cd ms-infra-ms/mecanica-fiap
docker compose -f docker-compose.full.yml up --build

# Buscar execução de uma OS
curl -s http://localhost/api/execucoes/os/{osId} \
  -H "Authorization: Bearer {token}"
```

## Testes

```bash
./mvnw test                       # unitários + BDD
./mvnw test -Dtest="CucumberTest" # apenas BDD
```

O BDD usa **Testcontainers** (MongoDB real) + `@MockBean` no `WorkshopEventPublisher` — requer Docker em execução.

## Tech stack

| | |
|-|-|
| **Java** | 21 |
| **Framework** | Spring Boot 3.5.x |
| **Banco** | MongoDB 7 (porta 27017) |
| **Mensageria** | RabbitMQ 3.13 |
| **Segurança** | JWT (JJWT 0.12) |
| **Porta** | 8083 |
| **Cobertura** | JaCoCo ≥ 80% |
| **BDD** | Cucumber 7.21 + JUnit Platform Suite |
