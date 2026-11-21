# Serviço de Reengajamento de Estudantes

Projeto acadêmico de Engenharia de Software II.
Link para o vídeo do Youtube: https://youtu.be/WKB6Q5xXZaA

## Objetivo
Identificar estudantes com atividades próximas do prazo de entrega e que ainda não realizaram a submissão, enviando lembretes por e-mail.

## Tecnologias
- Java 21
- Spring Boot
- Gradle
- Moodle REST API
- OpenFeign
- Docker
- RabbitMQ (planejado)
- Mailpit (planejado)

## Serviços
| Serviço | Pasta | Porta | Descrição |
|---|---|---|---|
| Acompanhamento | `acompanhamento-service/` | 8090 | consulta o Moodle e identifica alunos pendentes (publicação na fila: em andamento) |
| Notificações | `notificacoes-service/` | 8091 | consome a fila, envia o e-mail (SMTP) e registra o resultado — veja o [README do serviço](notificacoes-service/README.md) |

Infraestrutura (Docker): `docker compose up --build` sobe Moodle, RabbitMQ (painel em :15672), Mailpit (caixa de entrada em :8025) e o serviço de notificações.
