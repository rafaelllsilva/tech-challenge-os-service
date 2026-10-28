# Contrato de eventos — os-service

Código: `app/src/main/java/com/fiap/techchallenge/os/mensageria/contrato`. O contrato é uma cópia local do pacote, mantida em sincronia com billing-service e execution-service.

## Envelope

Todo evento trafega neste envelope (`EventEnvelope<T>`):

| Campo | Tipo | Descrição |
|---|---|---|
| `eventId` | UUID | Identificador único; usado na idempotência dos consumidores e como PK da outbox |
| `type` | string | Valor de `TipoEvento` |
| `version` | int | Versão do contrato (atualmente `1`) |
| `occurredAt` | ISO-8601 (UTC) | Instante em que o evento ocorreu |
| `correlationId` | string | `osId`, para rastrear a saga |
| `payload` | objeto | Depende do `type` |

Tipos desconhecidos geram `EventoDesconhecidoException` no `EventoSerializer`.

## Catálogo

| Evento | Tópico | Produtor | Consumidores | Payload |
|---|---|---|---|---|
| `OS_CRIADA` | `os-events` | os-service | billing, execution | `osId, clienteId, veiculoId, solicitacao, criadaEm` |
| `ORCAMENTO_APROVADO` | `os-events` | os-service | billing | `osId, orcamentoId` |
| `ORCAMENTO_REJEITADO` | `os-events` | os-service | billing | `osId, orcamentoId, motivo` |
| `OS_CANCELADA` | `os-events` | os-service | billing, execution | `osId, motivo, statusAnterior` |
| `ORCAMENTO_GERADO` | `billing-events` | billing | os-service | `osId, orcamentoId, valorTotal` |
| `PAGAMENTO_CONFIRMADO` | `billing-events` | billing | os-service, execution | `osId, pagamentoId, valor` |
| `PAGAMENTO_FALHOU` | `billing-events` | billing | os-service | `osId, pagamentoId, motivo` |
| `PAGAMENTO_ESTORNADO` | `billing-events` | billing | os-service | `osId, pagamentoId` |
| `DIAGNOSTICO_CONCLUIDO` | `execution-events` | execution | billing, os-service (só registra) | `osId` |
| `ESTOQUE_RESERVADO` | `execution-events` | execution | os-service (só registra) | `osId` |
| `EXECUCAO_INICIADA` | `execution-events` | execution | os-service | `osId, iniciadaEm` |
| `EXECUCAO_FINALIZADA` | `execution-events` | execution | os-service | `osId, finalizadaEm` |
| `EXECUCAO_FALHOU` | `execution-events` | execution | os-service, billing | `osId, motivo` |

> `DIAGNOSTICO_CONCLUIDO` e `ESTOQUE_RESERVADO` têm só o `osId` por enquanto: o plano não define os campos, e o os-service apenas os registra. O desserializador ignora campos desconhecidos, então produtores podem acrescentar campos sem quebrar o os-service.

## Exemplos

`OS_CRIADA`
```json
{
  "eventId": "7f1c1a52-6c6f-4a8e-9a5d-3c1f0d2b9b11",
  "type": "OS_CRIADA",
  "version": 1,
  "occurredAt": "2026-01-01T10:00:00Z",
  "correlationId": "42",
  "payload": { "osId": "42", "clienteId": "1", "veiculoId": "2", "solicitacao": "Trocar óleo", "criadaEm": "2026-01-01T10:00:00Z" }
}
```

`ORCAMENTO_GERADO`
```json
{
  "eventId": "0b9d2c8e-1d6f-4f0e-8d2b-5a7f2c4e9a10",
  "type": "ORCAMENTO_GERADO",
  "version": 1,
  "occurredAt": "2026-01-01T10:05:00Z",
  "correlationId": "42",
  "payload": { "osId": "42", "orcamentoId": "orc-1", "valorTotal": 350.5 }
}
```

`PAGAMENTO_FALHOU`
```json
{
  "eventId": "c3a1e0f4-9b2d-4c55-a6e1-2f8d7b1c0a33",
  "type": "PAGAMENTO_FALHOU",
  "version": 1,
  "occurredAt": "2026-01-01T10:10:00Z",
  "correlationId": "42",
  "payload": { "osId": "42", "pagamentoId": "pag-1", "motivo": "cartão recusado" }
}
```
