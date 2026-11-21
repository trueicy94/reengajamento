package br.com.reengajamento.notificacoes.dominio;

import java.time.Instant;

/** Registro do resultado do processamento de uma solicitação. */
public record Notificacao(
        String requestId,
        StatusNotificacao status,
        int tentativas,
        String ultimoErro,
        String emailAluno,
        String nomeTarefa,
        Instant atualizadoEm) {
}
