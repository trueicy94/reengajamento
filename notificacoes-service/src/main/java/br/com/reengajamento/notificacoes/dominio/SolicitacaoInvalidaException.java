package br.com.reengajamento.notificacoes.dominio;

/** Mensagem da fila que não segue o contrato esperado. */
public class SolicitacaoInvalidaException extends RuntimeException {

    public SolicitacaoInvalidaException(String mensagem) {
        super(mensagem);
    }

    public SolicitacaoInvalidaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
