package br.com.reengajamento.acompanhamento.service;

import br.com.reengajamento.acompanhamento.client.MoodleClient;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

@Service
public class MoodleService {

    private final MoodleClient moodleClient;

    @Value("${moodle.token}")
    private String token;

    public MoodleService(MoodleClient moodleClient) {
        this.moodleClient = moodleClient;
    }

    public JsonNode buscarAtividades(Long courseId){
        return moodleClient.getAssignmets(
                token,
                "mod_assign_get_assignments",
                "json",
                courseId
        );
    }

    public JsonNode buscarAlunos(Long courseId){
        return moodleClient.getEnrolledUsers(
                token,
                "core_enrol_get_enrolled_users",
                "json",
                courseId
        );
    }

    public JsonNode buscarEntregas(Long assignmentId){
        return moodleClient.getSubmissions(
                token,
                "mod_assign_get_submissions",
                "json",
                assignmentId
        );
    }
    
    public boolean estaDentroDasProximas24Horas(long dueDate){
        long agora = Instant.now().getEpochSecond();
        long limite = Instant.now().plus(24, ChronoUnit.HOURS).getEpochSecond();

        return dueDate >= agora && dueDate <= limite;
    }

    public JsonNode buscarAtividadesProximasDoPrazo(Long courseId){
        JsonNode resposta = buscarAtividades(courseId);
        JsonNode atividades = resposta.path("courses").get(0).path("assignments");

        var atividadesFiltradas = new ArrayList<JsonNode>();

        for(JsonNode atividade : atividades){
            long dueDate = atividade.path("duedate").asLong();
            System.out.println(Instant.ofEpochSecond(dueDate));

            if(estaDentroDasProximas24Horas(dueDate)) {
                atividadesFiltradas.add(atividade);
            }
        }
        return tools.jackson.databind.node.JsonNodeFactory.instance
                .arrayNode()
                .addAll(atividadesFiltradas);
    }

    public JsonNode buscarAlunosSemEntrega(Long courseId, Long assignmentId) {

        JsonNode alunos = buscarAlunos(courseId);
        JsonNode respostaEntregas = buscarEntregas(assignmentId);

        Set alunosComEntrega = new HashSet<>();

        for (JsonNode assignment : respostaEntregas.path("assignments")) {

            for (JsonNode submission : assignment.path("submissions")) {

                if ("submitted".equals(submission.path("status").asText())) {
                    alunosComEntrega.add(submission.path("userid").asLong());
                }
            }
        }

        var alunosSemEntrega =
                tools.jackson.databind.node.JsonNodeFactory.instance.arrayNode();

        for (JsonNode aluno : alunos) {

            boolean ehAluno = false;

            for (JsonNode role : aluno.path("roles")) {
                if ("student".equals(role.path("shortname").asText())) {
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
}