package br.com.reengajamento.notificacoes.aplicacao;

import br.com.reengajamento.notificacoes.dominio.Notificacao;
import br.com.reengajamento.notificacoes.dominio.SolicitacaoReengajamento;
import br.com.reengajamento.notificacoes.dominio.StatusNotificacao;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Dublês simples (sem Mockito) usados nos testes do núcleo. */
final class FakesParaTeste {

    private FakesParaTeste() {
    }

    static SolicitacaoReengajamento solicitacao(long aluno) {
        return SolicitacaoReengajamento.criar(
                "2:1:" + aluno, 2, 1, aluno, "Ana Teste", "ana" + aluno + "@aluno.test",
                "Trabalho 1 - Modelagem", 1_795_402_740L, "http://localhost:8080/mod/assign/view.php?id=101");
    }

    static class EnviadorFake implements EnviadorEmail {
        final List<SolicitacaoReengajamento> enviados = new ArrayList<>();
        int falhasRestantes;

        EnviadorFake(int falhasRestantes) {
            this.falhasRestantes = falhasRestantes;
        }

        @Override
        public void enviar(SolicitacaoReengajamento s) {
            if (falhasRestantes > 0) {
                falhasRestantes--;
                throw new IllegalStateException("SMTP indisponível");
            }
            enviados.add(s);
        }
    }

    static class RegistroEmMemoria implements RegistroNotificacoes {
        final Map<String, Notificacao> dados = new LinkedHashMap<>();

        @Override
        public Optional<Notificacao> buscar(String requestId) {
            return Optional.ofNullable(dados.get(requestId));
        }

        @Override
        public void registrarEnvio(SolicitacaoReengajamento s) {
            int t = buscar(s.requestId()).map(Notificacao::tentativas).orElse(0) + 1;
            dados.put(s.requestId(), new Notificacao(s.requestId(), StatusNotificacao.ENVIADA, t, null,
                    s.studentEmail(), s.assignmentName(), Instant.now()));
        }

        @Override
        public void registrarFalha(SolicitacaoReengajamento s, String erro, boolean definitiva) {
            int t = buscar(s.requestId()).map(Notificacao::tentativas).orElse(0) + 1;
            dados.put(s.requestId(), new Notificacao(s.requestId(),
                    definitiva ? StatusNotificacao.FALHA : StatusNotificacao.PENDENTE, t, erro,
                    s.studentEmail(), s.assignmentName(), Instant.now()));
        }

        @Override
        public List<Notificacao> listar() {
            return new ArrayList<>(dados.values());
        }
    }
}
