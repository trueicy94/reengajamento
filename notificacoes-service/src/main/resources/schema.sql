CREATE TABLE IF NOT EXISTS notificacoes (
    request_id     VARCHAR(100)  PRIMARY KEY,
    status         VARCHAR(20)   NOT NULL,
    tentativas     INT           NOT NULL,
    ultimo_erro    VARCHAR(1000),
    email_aluno    VARCHAR(255),
    nome_tarefa    VARCHAR(255),
    criado_em      TIMESTAMP     NOT NULL,
    atualizado_em  TIMESTAMP     NOT NULL,
    enviado_em     TIMESTAMP
);
