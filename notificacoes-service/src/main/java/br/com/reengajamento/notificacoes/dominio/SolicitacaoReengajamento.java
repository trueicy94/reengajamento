package br.com.reengajamento.notificacoes.dominio;

/**
 * Solicitação de reengajamento publicada pelo Acompanhamento na fila.
 *
 * @param requestId      identificador de idempotência: "curso:tarefa:aluno"
 * @param dueDate        prazo da tarefa, em segundos desde 1970 (epoch), como o Moodle devolve
 */
public record SolicitacaoReengajamento(
        String requestId,
        long courseId,
        long assignmentId,
        long userId,
        String studentName,
        String studentEmail,
        String assignmentName,
        long dueDate,
        String link) {

    /** Cria validando os campos obrigatórios do contrato. */
    public static SolicitacaoReengajamento criar(
            String requestId,
            long courseId,
            long assignmentId,
            long userId,
            String studentName,
            String studentEmail,
            String assignmentName,
            long dueDate,
            String link) {

        if (vazio(requestId)) {
            throw new SolicitacaoInvalidaException("requestId obrigatório");
        }
        if (vazio(studentEmail) || !studentEmail.contains("@")) {
            throw new SolicitacaoInvalidaException("studentEmail obrigatório e válido");
        }
        if (vazio(assignmentName)) {
            throw new SolicitacaoInvalidaException("assignmentName obrigatório");
        }
        if (dueDate <= 0) {
            throw new SolicitacaoInvalidaException("dueDate obrigatório (epoch em segundos)");
        }
        if (vazio(link)) {
            throw new SolicitacaoInvalidaException("link obrigatório");
        }
        return new SolicitacaoReengajamento(
                requestId.trim(), courseId, assignmentId, userId,
                studentName == null ? "" : studentName.trim(),
                studentEmail.trim(), assignmentName.trim(), dueDate, link.trim());
    }

    private static boolean vazio(String s) {
        return s == null || s.isBlank();
    }
}
