# Testes do notificacoes-service

## 1. Automatizados (`./gradlew test`)
Testes JUnit 5 do núcleo (sem RabbitMQ/SMTP; usam dublês em memória):

| Critério | Teste |
|---|---|
| Envia e registra | `ProcessadorSolicitacaoTest.enviaLembreteERegistraResultado` |
| CA3 – reprocessar não reenvia | `ca3_reprocessarSolicitacaoConcluidaNaoReenvia`, `alunosDiferentesNaoSeAfetam` |
| CA4 – falha registrada e nova tentativa | `ca4_falhaERegistradaEGeraNovaTentativa` |
| CA4 – tentativas esgotadas | `ca4_tentativasEsgotadasViramFalhaDefinitiva` |
| E-mail com nome da tarefa, prazo e link (CA1) | `SolicitacaoEMensagemTest.emailTemNomeDaTarefaPrazoELink` |
| Validação do contrato | `camposObrigatoriosSaoValidados` |

Registro da execução: _preencher com a saída do `./gradlew test` (anexar print/trecho)._

## 2. Ponta a ponta (docker compose) — preencher na execução
`docker compose up --build rabbitmq mailpit notificacoes-service` e publique mensagens conforme o README.

| # | Passo | Esperado | Resultado / evidência |
|---|---|---|---|
| N1 | Publicar a solicitação `2:1:11` | 1 e-mail em http://localhost:8025 com nome da tarefa, prazo e link; `GET /api/notificacoes` mostra `ENVIADA` | _preencher_ |
| N2 | Publicar a **mesma** solicitação de novo (2–3×) | continua 1 e-mail; log "já enviada; ignorando duplicata" | _preencher_ |
| N3 | `docker compose stop mailpit`; publicar `2:1:13` | log de falha; status `PENDENTE`, tentativas 1; mensagem em `reengajamento.retry` | _preencher_ |
| N4 | `docker compose start mailpit` e aguardar ~30 s | e-mail chega; status `ENVIADA`, tentativas 2 | _preencher_ |
| N5 | Parar o Mailpit e aguardar 3 falhas | status `FALHA`; mensagem em `reengajamento.falhas` | _preencher_ |
| N6 | Publicar `{"lixo": true}` | mensagem em `reengajamento.falhas`, sem derrubar o serviço | _preencher_ |
| N7 | `docker compose restart notificacoes-service` e republicar `2:1:11` | não reenvia (registro persistido no volume) | _preencher_ |
