package br.com.reengajamento.notificacoes.aplicacao;

import static br.com.reengajamento.notificacoes.aplicacao.FakesParaTeste.solicitacao;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.reengajamento.notificacoes.aplicacao.FakesParaTeste.EnviadorFake;
import br.com.reengajamento.notificacoes.aplicacao.FakesParaTeste.RegistroEmMemoria;
import br.com.reengajamento.notificacoes.dominio.ResultadoProcessamento;
import br.com.reengajamento.notificacoes.dominio.StatusNotificacao;
import org.junit.jupiter.api.Test;

/** Critérios de aceite 3 e 4 do projeto, no nível do serviço de notificações. */
class ProcessadorSolicitacaoTest {

    @Test
    void enviaLembreteERegistraResultado() {
        var enviador = new EnviadorFake(0);
        var registro = new RegistroEmMemoria();
        var processador = new ProcessadorSolicitacao(enviador, registro, 3);

        assertEquals(ResultadoProcessamento.ENVIADO, processador.processar(solicitacao(11)));

        assertEquals(1, enviador.enviados.size());
        var n = registro.buscar("2:1:11").orElseThrow();
        assertEquals(StatusNotificacao.ENVIADA, n.status());
        assertEquals(1, n.tentativas());
    }

    @Test
    void ca3_reprocessarSolicitacaoConcluidaNaoReenvia() {
        var enviador = new EnviadorFake(0);
        var processador = new ProcessadorSolicitacao(enviador, new RegistroEmMemoria(), 3);

        processador.processar(solicitacao(11));
        assertEquals(ResultadoProcessamento.DUPLICADA, processador.processar(solicitacao(11)));
        assertEquals(ResultadoProcessamento.DUPLICADA, processador.processar(solicitacao(11)));

        assertEquals(1, enviador.enviados.size()); // nenhum envio novo
    }

    @Test
    void alunosDiferentesNaoSeAfetam() {
        var enviador = new EnviadorFake(0);
        var processador = new ProcessadorSolicitacao(enviador, new RegistroEmMemoria(), 3);

        processador.processar(solicitacao(11));
        assertEquals(ResultadoProcessamento.ENVIADO, processador.processar(solicitacao(13)));
        assertEquals(2, enviador.enviados.size());
    }

    @Test
    void ca4_falhaERegistradaEGeraNovaTentativa() {
        var enviador = new EnviadorFake(1); // falha só na 1ª vez
        var registro = new RegistroEmMemoria();
        var processador = new ProcessadorSolicitacao(enviador, registro, 3);

        assertEquals(ResultadoProcessamento.NOVA_TENTATIVA, processador.processar(solicitacao(11)));
        var aposFalha = registro.buscar("2:1:11").orElseThrow();
        assertEquals(StatusNotificacao.PENDENTE, aposFalha.status());
        assertEquals(1, aposFalha.tentativas());
        assertTrue(aposFalha.ultimoErro().contains("SMTP indisponível"));

        // a fila devolve a mensagem após o atraso e a nova tentativa funciona
        assertEquals(ResultadoProcessamento.ENVIADO, processador.processar(solicitacao(11)));
        var aposSucesso = registro.buscar("2:1:11").orElseThrow();
        assertEquals(StatusNotificacao.ENVIADA, aposSucesso.status());
        assertEquals(2, aposSucesso.tentativas());
    }

    @Test
    void ca4_tentativasEsgotadasViramFalhaDefinitiva() {
        var enviador = new EnviadorFake(99);
        var registro = new RegistroEmMemoria();
        var processador = new ProcessadorSolicitacao(enviador, registro, 3);

        assertEquals(ResultadoProcessamento.NOVA_TENTATIVA, processador.processar(solicitacao(11)));
        assertEquals(ResultadoProcessamento.NOVA_TENTATIVA, processador.processar(solicitacao(11)));
        assertEquals(ResultadoProcessamento.FALHA_DEFINITIVA, processador.processar(solicitacao(11)));

        assertEquals(StatusNotificacao.FALHA, registro.buscar("2:1:11").orElseThrow().status());
        assertEquals(0, enviador.enviados.size());
    }
}
