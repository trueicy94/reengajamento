package br.com.reengajamento.notificacoes.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class SolicitacaoEMensagemTest {

    private static SolicitacaoReengajamento valida() {
        // 20/11/2026 23:59 em America/Sao_Paulo (UTC-3) = 21/11/2026 02:59 UTC
        return SolicitacaoReengajamento.criar("2:1:11", 2, 1, 11, "Ana Teste", "ana@aluno.test",
                "Trabalho 1 - Modelagem", 1_795_229_940L, "http://localhost:8080/mod/assign/view.php?id=101");
    }

    @Test
    void emailTemNomeDaTarefaPrazoELink() {
        var s = valida();
        String corpo = MensagemLembrete.corpo(s, ZoneId.of("America/Sao_Paulo"));

        assertTrue(corpo.contains("Olá, Ana Teste!"));
        assertTrue(corpo.contains("\"Trabalho 1 - Modelagem\""));
        assertTrue(corpo.contains("20/11/2026 às 23:59"), corpo);
        assertTrue(corpo.contains("http://localhost:8080/mod/assign/view.php?id=101"));
        assertTrue(MensagemLembrete.assunto(s).contains("Trabalho 1 - Modelagem"));
    }

    @Test
    void semNomeUsaSaudacaoGenerica() {
        var s = SolicitacaoReengajamento.criar("2:1:11", 2, 1, 11, null, "ana@aluno.test",
                "T1", 1_795_229_940L, "http://x/y");
        assertTrue(MensagemLembrete.corpo(s, ZoneId.of("UTC")).startsWith("Olá, estudante!"));
    }

    @Test
    void camposObrigatoriosSaoValidados() {
        assertThrows(SolicitacaoInvalidaException.class, () -> SolicitacaoReengajamento.criar(
                " ", 2, 1, 11, "A", "a@b.c", "T", 1L, "l"));
        assertThrows(SolicitacaoInvalidaException.class, () -> SolicitacaoReengajamento.criar(
                "id", 2, 1, 11, "A", "sem-arroba", "T", 1L, "l"));
        assertThrows(SolicitacaoInvalidaException.class, () -> SolicitacaoReengajamento.criar(
                "id", 2, 1, 11, "A", "a@b.c", "", 1L, "l"));
        assertThrows(SolicitacaoInvalidaException.class, () -> SolicitacaoReengajamento.criar(
                "id", 2, 1, 11, "A", "a@b.c", "T", 0L, "l"));
        assertThrows(SolicitacaoInvalidaException.class, () -> SolicitacaoReengajamento.criar(
                "id", 2, 1, 11, "A", "a@b.c", "T", 1L, null));
    }

    @Test
    void identificadorEPrazoSaoPreservados() {
        var s = valida();
        assertEquals("2:1:11", s.requestId());
        assertEquals(1_795_229_940L, s.dueDate());
    }
}
