# Proteção da branch `main`

A `main` só deve receber código por Pull Request, com uma aprovação e com os checks do
`.github/workflows/pr.yml` verdes.

## Regras

| Regra | Valor |
|---|---|
| Pull Request obrigatório | sim |
| Aprovações necessárias | 1 |
| Descartar aprovações antigas após novo push | sim |
| Branch atualizada antes do merge (strict) | sim |
| Required checks | `Testes e Cobertura (JaCoCo)`, `SonarQube Quality Gate`, `Validar Build da Imagem Docker` |
| Aplicar também a administradores | sim |
| Push direto e force push | bloqueados |

Os nomes dos checks são os `name:` dos jobs de `pr.yml`. Se um job for renomeado, atualize a regra.

## Pela interface

Settings → Branches → Add branch ruleset (ou Branch protection rule) para `main` e marque as opções da tabela.

## Pela linha de comando (não executado)

Substitua `OWNER/REPO` pelo repositório. O comando exige permissão de administrador.

```bash
gh api -X PUT repos/OWNER/REPO/branches/main/protection \
  -H "Accept: application/vnd.github+json" \
  --input - <<'JSON'
{
  "required_status_checks": {
    "strict": true,
    "contexts": [
      "Testes e Cobertura (JaCoCo)",
      "SonarQube Quality Gate",
      "Validar Build da Imagem Docker"
    ]
  },
  "enforce_admins": true,
  "required_pull_request_reviews": {
    "required_approving_review_count": 1,
    "dismiss_stale_reviews": true
  },
  "restrictions": null,
  "allow_force_pushes": false,
  "allow_deletions": false
}
JSON
```

## Secrets necessários no repositório

`SONAR_TOKEN` e `SONAR_HOST_URL` (job de Sonar), mais os secrets de AWS e banco já usados pelo
`deploy.yml`.
