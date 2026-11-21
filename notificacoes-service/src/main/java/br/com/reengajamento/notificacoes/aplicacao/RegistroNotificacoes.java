package br.com.reengajamento.notificacoes.aplicacao;

import br.com.reengajamento.notificacoes.dominio.Notificacao;
import br.com.reengajamento.notificacoes.dominio.SolicitacaoReengajamento;
import java.util.List;
import java.util.Optional;

/** Porta de saída: guarda o resultado de cada solicitação (base da idempotência). */
public interface RegistroNotificacoes {

    Optional<Notificacao> buscar(String requestId);

    void registrarEnvio(SolicitacaoReengajamento solicitacao);

    void registrarFalha(SolicitacaoReengajamento solicitacao, String erro, boolean definitiva);

    List<Notificacao> listar();
}
