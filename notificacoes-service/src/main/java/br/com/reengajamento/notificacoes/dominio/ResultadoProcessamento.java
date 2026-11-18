package br.com.reengajamento.notificacoes.dominio;

public enum ResultadoProcessamento {
    ENVIADO,
    /** já enviada antes: nada a fazer */
    DUPLICADA,
    /** falhou, mas ainda há tentativas: vai para a fila de espera */
    NOVA_TENTATIVA,
    /** falhou e esgotou as tentativas: vai para a fila de falhas */
    FALHA_DEFINITIVA
}
