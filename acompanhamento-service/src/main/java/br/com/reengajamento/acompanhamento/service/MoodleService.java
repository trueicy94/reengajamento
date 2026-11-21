package br.com.reengajamento.acompanhamento.service;

import br.com.reengajamento.acompanhamento.client.MoodleClient;
import br.com.reengajamento.acompanhamento.dto.SolicitacaoNotificacao;
import br.com.reengajamento.acompanhamento.rabbit.NotificacaoPublisher;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import tools.jackson.databind.JsonNode;

@Service
public class MoodleService {

    private final MoodleClient moodleClient;
    private final NotificacaoPublisher notificacaoPublisher;

    @Value("${moodle.token}")
    private String token;

    public MoodleService(
            MoodleClient moodleClient,
            NotificacaoPublisher notificacaoPublisher) {
        this.moodleClient = moodleClient;
        this.notificacaoPublisher = notificacaoPublisher;
    }

    public JsonNode buscarAtividades(Long courseId) {
        return moodleClient.getAssignmets(
                token,
                "mod_assign_get_assignments",
                "json",
                courseId
        );
    }

    public JsonNode buscarAlunos(Long courseId) {
        return moodleClient.getEnrolledUsers(
                token,
                "core_enrol_get_enrolled_users",
                "json",
                courseId
        );
    }

    public JsonNode buscarEntregas(Long assignmentId) {
        return moodleClient.getSubmissions(
                token,
                "mod_assign_get_submissions",
                "json",
                assignmentId
        );
    }

    public boolean estaDentroDasProximas24Horas(long dueDate) {
        long agora = Instant.now().getEpochSecond();
        long limite = Instant.now()
                .plus(24, ChronoUnit.HOURS)
                .getEpochSecond();

        return dueDate >= agora && dueDate <= limite;
    }

    public JsonNode buscarAtividadesProximasDoPrazo(Long courseId) {
        JsonNode resposta = buscarAtividades(courseId);
        JsonNode atividades = resposta.path("courses")
                .get(0)
                .path("assignments");

        var atividadesFiltradas = new ArrayList<JsonNode>();

        for (JsonNode atividade : atividades) {
            long dueDate = atividade.path("duedate").asLong();

            if (estaDentroDasProximas24Horas(dueDate)) {
                atividadesFiltradas.add(atividade);
            }
        }

        return tools.jackson.databind.node.JsonNodeFactory.instance
                .arrayNode()
                .addAll(atividadesFiltradas);
    }

    public JsonNode buscarAlunosSemEntrega(
            Long courseId,
            Long assignmentId) {

        JsonNode alunos = buscarAlunos(courseId);
        JsonNode respostaEntregas = buscarEntregas(assignmentId);

        Set<Long> alunosComEntrega = new HashSet<>();

        for (JsonNode assignment :
                respostaEntregas.path("assignments")) {

            for (JsonNode submission :
                    assignment.path("submissions")) {

                if ("submitted".equals(
                        submission.path("status").asText())) {

                    alunosComEntrega.add(
                            submission.path("userid").asLong());
                }
            }
        }

        var alunosSemEntrega =
                tools.jackson.databind.node.JsonNodeFactory.instance
                        .arrayNode();

        for (JsonNode aluno : alunos) {

            boolean ehAluno = false;

            for (JsonNode role : aluno.path("roles")) {
                if ("student".equals(
                        role.path("shortname").asText())) {

                    ehAluno = true;
                    break;
                }
            }

            long alunoId = aluno.path("id").asLong();

            if (ehAluno && !alunosComEntrega.contains(alunoId)) {
                alunosSemEntrega.add(aluno);
            }
        }

        return alunosSemEntrega;
    }

    public void processarReengajamento(Long courseId) {

        JsonNode atividades =
                buscarAtividadesProximasDoPrazo(courseId);

        for (JsonNode atividade : atividades) {

            Long atividadeId = atividade.path("id").asLong();
            String nomeAtividade =
                    atividade.path("name").asText();

            long dueDate = atividade.path("duedate").asLong();

            LocalDateTime dataVencimento =
                    LocalDateTime.ofInstant(
                            Instant.ofEpochSecond(dueDate),
                            ZoneId.of("America/Sao_Paulo")
                    );

            JsonNode alunosSemEntrega =
                    buscarAlunosSemEntrega(courseId, atividadeId);

            for (JsonNode aluno : alunosSemEntrega) {

                Long alunoId = aluno.path("id").asLong();

                String nomeAluno =
                        aluno.path("fullname").asText();

                String emailAluno =
                        aluno.path("email").asText();

                SolicitacaoNotificacao solicitacao = new SolicitacaoNotificacao(
                        String.valueOf(courseId+atividadeId+alunoId),
                        courseId,
                        atividadeId,
                        alunoId,
                        nomeAluno,
                        emailAluno,
                        nomeAtividade,
                        dueDate,
                        "http://localhost:8080/mod/assign/view.php?id=" + atividadeId
                );

                notificacaoPublisher.enviarNotificacao(solicitacao);
            }
        }
    }
}