package br.com.reengajamento.notificacoes.aplicacao;

import br.com.reengajamento.notificacoes.dominio.SolicitacaoReengajamento;

/** Porta de saída: envia o lembrete. Lança exceção se o envio falhar. */
public interface EnviadorEmail {

    void enviar(SolicitacaoReengajamento solicitacao);
}
