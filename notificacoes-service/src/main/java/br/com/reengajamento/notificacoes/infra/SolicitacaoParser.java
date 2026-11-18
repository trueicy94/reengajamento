package br.com.reengajamento.notificacoes.infra;

import br.com.reengajamento.notificacoes.dominio.SolicitacaoInvalidaException;
import br.com.reengajamento.notificacoes.dominio.SolicitacaoReengajamento;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Converte o JSON da fila na solicitação. Contrato (camelCase):
 * requestId, courseId, assignmentId, userId, studentName, studentEmail,
 * assignmentName, dueDate (epoch em segundos), link.
 */
@Component
public class SolicitacaoParser {

    private final JsonMapper mapper = JsonMapper.builder().build();

    public SolicitacaoReengajamento parse(String json) {
        JsonNode raiz;
        try {
            raiz = mapper.readTree(json);
        } catch (RuntimeException e) {
            throw new SolicitacaoInvalidaException("JSON inválido", e);
        }
        if (raiz == null || !raiz.isObject()) {
            throw new SolicitacaoInvalidaException("a mensagem deve ser um objeto JSON");
        }
        return SolicitacaoReengajamento.criar(
                texto(raiz, "requestId"),
                raiz.path("courseId").asLong(),
                raiz.path("assignmentId").asLong(),
                raiz.path("userId").asLong(),
                texto(raiz, "studentName"),
                texto(raiz, "studentEmail"),
                texto(raiz, "assignmentName"),
                raiz.path("dueDate").asLong(),
                texto(raiz, "link"));
    }

    private static String texto(JsonNode no, String campo) {
        JsonNode valor = no.path(campo);
        return valor.isMissingNode() || valor.isNull() ? null : valor.asText();
    }
}
