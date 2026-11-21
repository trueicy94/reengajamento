package br.com.reengajamento.notificacoes.infra;

import br.com.reengajamento.notificacoes.aplicacao.RegistroNotificacoes;
import br.com.reengajamento.notificacoes.dominio.Notificacao;
import br.com.reengajamento.notificacoes.dominio.SolicitacaoReengajamento;
import br.com.reengajamento.notificacoes.dominio.StatusNotificacao;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/** Registro dos resultados em banco H2 (arquivo em volume Docker). */
@Repository
public class RegistroNotificacoesJdbc implements RegistroNotificacoes {

    private static final RowMapper<Notificacao> MAPPER = (ResultSet rs, int i) -> mapear(rs);

    private final JdbcTemplate jdbc;

    public RegistroNotificacoesJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static Notificacao mapear(ResultSet rs) throws SQLException {
        return new Notificacao(
                rs.getString("request_id"),
                StatusNotificacao.valueOf(rs.getString("status")),
                rs.getInt("tentativas"),
                rs.getString("ultimo_erro"),
                rs.getString("email_aluno"),
                rs.getString("nome_tarefa"),
                rs.getTimestamp("atualizado_em").toInstant());
    }

    @Override
    public Optional<Notificacao> buscar(String requestId) {
        return jdbc.query("SELECT * FROM notificacoes WHERE request_id = ?", MAPPER, requestId)
                .stream().findFirst();
    }

    @Override
    public void registrarEnvio(SolicitacaoReengajamento s) {
        gravar(s, StatusNotificacao.ENVIADA, null, true);
    }

    @Override
    public void registrarFalha(SolicitacaoReengajamento s, String erro, boolean definitiva) {
        gravar(s, definitiva ? StatusNotificacao.FALHA : StatusNotificacao.PENDENTE, erro, false);
    }

    @Override
    public List<Notificacao> listar() {
        return jdbc.query("SELECT * FROM notificacoes ORDER BY atualizado_em DESC", MAPPER);
    }

    private void gravar(SolicitacaoReengajamento s, StatusNotificacao status, String erro, boolean enviada) {
        Timestamp agora = Timestamp.from(Instant.now());
        Timestamp enviadoEm = enviada ? agora : null;
        int atualizadas = jdbc.update(
                "UPDATE notificacoes SET status = ?, tentativas = tentativas + 1, ultimo_erro = ?, "
                        + "atualizado_em = ?, enviado_em = ? WHERE request_id = ?",
                status.name(), erro, agora, enviadoEm, s.requestId());
        if (atualizadas == 0) {
            jdbc.update(
                    "INSERT INTO notificacoes (request_id, status, tentativas, ultimo_erro, email_aluno, "
                            + "nome_tarefa, criado_em, atualizado_em, enviado_em) VALUES (?, ?, 1, ?, ?, ?, ?, ?, ?)",
                    s.requestId(), status.name(), erro, s.studentEmail(), s.assignmentName(),
                    agora, agora, enviadoEm);
        }
    }
}
