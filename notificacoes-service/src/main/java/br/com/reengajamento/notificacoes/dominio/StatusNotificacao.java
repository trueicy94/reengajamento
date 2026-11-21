package br.com.reengajamento.notificacoes.dominio;

public enum StatusNotificacao {
    /** houve falha e ainda há tentativas */
    PENDENTE,
    /** e-mail enviado com sucesso */
    ENVIADA,
    /** tentativas esgotadas */
    FALHA
}
