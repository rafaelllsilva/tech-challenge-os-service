# Evidências e entregáveis — os-service (Fase 4)

Checklist para o grupo preencher antes da entrega. Marque cada item e cole o link ou a evidência.

| Entregável | Onde está / como obter | Evidência (link ou arquivo) | Feito |
|---|---|---|---|
| Repositório do os-service | URL do GitHub | _preencher_ | [ ] |
| Repositório do billing-service | URL do GitHub | _preencher_ | [ ] |
| Repositório do execution-service | URL do GitHub | _preencher_ | [ ] |
| Cobertura de testes ≥ 80% | `./gradlew :app:check` e `app/build/reports/jacoco/test/html/index.html` | _print do relatório_ | [ ] |
| BDD (fluxo feliz e compensação) passando | `app/src/test/resources/features/`; relatório de testes do CI | _print ou link da execução_ | [ ] |
| SonarQube: quality gate aprovado | Painel do projeto `tech-challenge-os-service` | _print do painel_ | [ ] |
| Pipeline de PR verde | GitHub Actions, workflow `CI - Pull Request` | _link da execução_ | [ ] |
| Pipeline de deploy verde | GitHub Actions, workflow `CD - Build, Push & Deploy EKS` | _link da execução_ | [ ] |
| Proteção da branch `main` | `docs/PROTECAO-BRANCH.md`; Settings → Branches | _print da regra_ | [ ] |
| Recursos SNS/SQS criados na AWS | `infra/aws/criar-recursos-mensageria.sh` | _print do console ou da saída do script_ | [ ] |
| Deploy no EKS funcionando | `kubectl get pods -n tech-challenge` | _print_ | [ ] |
| Swagger no ar | `http://<HOST>/swagger-ui/index.html` | _link ou print_ | [ ] |
| Coleção Postman | `postman/Tech Challenge Fase 4 - OS Service.postman_collection.json` | _print da execução do fluxo_ | [ ] |
| Saga ponta a ponta demonstrada | Histórico da OS (`GET /api/ordemDeServico/{id}/historico`) e logs com `correlationId` | _print ou trecho dos logs_ | [ ] |
| Métricas da saga | `/actuator/prometheus` (`os_eventos_*`, `os_outbox_pendentes`, `os_saga_transicoes_total`) | _print_ | [ ] |
| Documento de arquitetura (PDF) | Diagramas e justificativa da saga (README) | _arquivo_ | [ ] |
| Vídeo de demonstração (até 15 min) | Fluxo feliz, compensação, pipeline e cobertura | _link_ | [ ] |
