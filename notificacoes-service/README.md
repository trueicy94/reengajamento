# notificacoes-service

Microsserviço **Notificações** do projeto de reengajamento: consome as solicitações publicadas pelo
`acompanhamento-service` na fila RabbitMQ, envia o lembrete por e-mail (SMTP; Mailpit nos testes) e registra o resultado.

Stack: Java 21, Spring Boot 4.1.2, Gradle, Spring AMQP, Spring Mail, JDBC + H2 (arquivo em volume).

## Como funciona

```
acompanhamento-service ──► [reengajamento.solicitacoes] ──► notificacoes-service ──SMTP──► Mailpit
                                   ▲                              │ falhou, ainda há tentativas
                                   └── [reengajamento.retry] ◄────┤ (espera 30 s e volta)
                                       [reengajamento.falhas] ◄───┘ tentativas esgotadas / mensagem inválida
```

| Situação | O que o serviço faz |
|---|---|
| Solicitação nova | envia o e-mail e grava `ENVIADA` no banco |
| `requestId` já `ENVIADA` (critério 3) | ignora; **não** reenvia |
| Falha no SMTP (critério 4) | grava `PENDENTE` + erro + nº de tentativas e manda para `reengajamento.retry`; após o atraso (`notificacoes.retry.atraso-ms`) a fila devolve a mensagem à principal |
| `notificacoes.max-tentativas` esgotadas | grava `FALHA` e manda para `reengajamento.falhas` |
| JSON inválido / campos faltando | manda para `reengajamento.falhas` |

O Acompanhamento pode publicar a mesma solicitação a cada ciclo (a cada hora, enquanto o aluno estiver pendente
na janela de 24 h): **a deduplicação é responsabilidade deste serviço**, pelo `requestId`.

## Contrato da mensagem (o que o Acompanhamento deve publicar)

Fila: `reengajamento.solicitacoes` (durável, sem argumentos extras). Corpo JSON, uma mensagem por aluno e tarefa:

```json
{
  "requestId": "2:1:11",
  "courseId": 2,
  "assignmentId": 1,
  "userId": 11,
  "studentName": "Ana Teste",
  "studentEmail": "ana@aluno.test",
  "assignmentName": "Trabalho 1 - Modelagem",
  "dueDate": 1795229940,
  "link": "http://localhost:8080/mod/assign/view.php?id=101"
}
```

- `requestId` = `courseId:assignmentId:userId` (identificador de idempotência).
- `dueDate` = `duedate` da tarefa no Moodle, **em segundos** (epoch), do jeito que a API devolve.
- `link` = `{moodle.url}/mod/assign/view.php?id={cmid}` (o `cmid` vem em `mod_assign_get_assignments`).
- `studentEmail` vem de `core_enrol_get_enrolled_users`.

Exemplo de publicação no `acompanhamento-service` (adicionar `spring-boot-starter-amqp`; o serviço de notificações
declara as filas, mas o Acompanhamento também pode declarar `QueueBuilder.durable("reengajamento.solicitacoes").build()`):

```java
rabbitTemplate.convertAndSend("", "reengajamento.solicitacoes", json); // json = String com o contrato acima
```

## Executar

Tudo (Moodle + RabbitMQ + Mailpit + notificações), a partir da raiz do repositório:
```bash
docker compose up --build
```
- Mailpit (e-mails recebidos): http://localhost:8025
- RabbitMQ (filas): http://localhost:15672 — `guest` / `guest`
- Registro dos resultados: http://localhost:8091/api/notificacoes
- Saúde: http://localhost:8091/actuator/health

Só o essencial para testar o notificador isoladamente:
```bash
docker compose up --build rabbitmq mailpit notificacoes-service
```

Local (sem Docker para o serviço; RabbitMQ e Mailpit no compose):
```bash
cd notificacoes-service
./gradlew test        # testes
./gradlew bootRun     # sobe na porta 8091
```

## Testar sem o Acompanhamento (publicando uma solicitação à mão)

No painel do RabbitMQ: **Queues → reengajamento.solicitacoes → Publish message**, em *Properties* coloque
`content_type = application/json` e cole o JSON acima em *Payload* (ajuste `dueDate` para daqui a algumas horas).
Ou, em bash/Git Bash:

```bash
DUE=$(( $(date +%s) + 18000 ))   # daqui a 5 h
PAYLOAD="{\"requestId\":\"2:1:11\",\"courseId\":2,\"assignmentId\":1,\"userId\":11,\"studentName\":\"Ana Teste\",\"studentEmail\":\"ana@aluno.test\",\"assignmentName\":\"Trabalho 1 - Modelagem\",\"dueDate\":$DUE,\"link\":\"http://localhost:8080/mod/assign/view.php?id=101\"}"
curl -u guest:guest -H "content-type: application/json" -X POST \
  http://localhost:15672/api/exchanges/%2F/amq.default/publish \
  -d "{\"properties\":{\"delivery_mode\":2,\"content_type\":\"application/json\"},\"routing_key\":\"reengajamento.solicitacoes\",\"payload\":$(printf '%s' "$PAYLOAD" | python3 -c 'import json,sys;print(json.dumps(sys.stdin.read()))'),\"payload_encoding\":\"string\"}"
```

Roteiro dos critérios de aceite: veja [TESTES.md](TESTES.md).

## Configuração (`application.properties`; em Docker use variáveis de ambiente)

| Propriedade | Padrão | Variável (Docker) |
|---|---|---|
| `spring.rabbitmq.host` | localhost | `SPRING_RABBITMQ_HOST` |
| `spring.mail.host` / `port` | localhost / 1025 | `SPRING_MAIL_HOST` / `SPRING_MAIL_PORT` |
| `spring.datasource.url` | `jdbc:h2:file:./data/notificacoes` | `SPRING_DATASOURCE_URL` (no Docker: `/data`, em volume) |
| `notificacoes.retry.atraso-ms` | 30000 | `NOTIFICACOES_RETRY_ATRASO_MS` |
| `notificacoes.max-tentativas` | 3 | `NOTIFICACOES_MAX_TENTATIVAS` |
| `notificacoes.email.remetente` | lembretes@moodle.local | `NOTIFICACOES_EMAIL_REMETENTE` |

## Organização do código

- `dominio/` e `aplicacao/`: regra de negócio em Java puro (`ProcessadorSolicitacao`: idempotência, falha, limite de tentativas), sem Spring.
- `infra/`: RabbitMQ (`SolicitacaoListener`, `RabbitConfig`), SMTP (`EnviadorEmailSmtp`), banco (`RegistroNotificacoesJdbc`), JSON (`SolicitacaoParser`).
- `controller/`: consulta do registro (`GET /api/notificacoes`).
