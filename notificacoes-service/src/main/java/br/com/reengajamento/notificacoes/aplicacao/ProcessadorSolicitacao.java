package br.com.reengajamento.notificacoes.aplicacao;

import br.com.reengajamento.notificacoes.dominio.ResultadoProcessamento;
import br.com.reengajamento.notificacoes.dominio.SolicitacaoReengajamento;
import br.com.reengajamento.notificacoes.dominio.StatusNotificacao;

/**
 * Regra de negócio do Notificações, sem dependência de RabbitMQ/SMTP/banco:
 * não reenviar o que já foi enviado, registrar falhas e limitar as tentativas.
 */
public class ProcessadorSolicitacao {

    private final EnviadorEmail enviador;
    private final RegistroNotificacoes registro;
    private final int maxTentativas;

    public ProcessadorSolicitacao(EnviadorEmail enviador, RegistroNotificacoes registro, int maxTentativas) {
        if (maxTentativas < 1) {
            throw new IllegalArgumentException("maxTentativas deve ser >= 1");
        }
        this.enviador = enviador;
        this.registro = registro;
        this.maxTentativas = maxTentativas;
    }

    public ResultadoProcessamento processar(SolicitacaoReengajamento solicitacao) {
        var existente = registro.buscar(solicitacao.requestId());

        // idempotência: identificador curso+tarefa+aluno já concluído -> não reenvia
        if (existente.isPresent() && existente.get().status() == StatusNotificacao.ENVIADA) {
            return ResultadoProcessamento.DUPLICADA;
        }

        try {
            enviador.enviar(solicitacao);
        } catch (Exception e) {
            int tentativa = existente.map(n -> n.tentativas()).orElse(0) + 1;
            boolean definitiva = tentativa >= maxTentativas;
            registro.registrarFalha(solicitacao, descrever(e), definitiva);
            return definitiva ? ResultadoProcessamento.FALHA_DEFINITIVA : ResultadoProcessamento.NOVA_TENTATIVA;
        }

        registro.registrarEnvio(solicitacao);
        return ResultadoProcessamento.ENVIADO;
    }

    private static String descrever(Exception e) {
        String texto = e.getClass().getSimpleName() + ": " + e.getMessage();
        return texto.length() > 900 ? texto.substring(0, 900) : texto;
    }
}
