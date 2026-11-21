package br.com.reengajamento.notificacoes.dominio;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** Texto do e-mail: nome da tarefa, prazo e link de acesso. */
public final class MensagemLembrete {

    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    private MensagemLembrete() {
    }

    public static String assunto(SolicitacaoReengajamento s) {
        return "Lembrete: \"" + s.assignmentName() + "\" vence em breve";
    }

    public static String corpo(SolicitacaoReengajamento s, ZoneId fuso) {
        String nome = s.studentName().isBlank() ? "estudante" : s.studentName();
        String prazo = FORMATO.format(Instant.ofEpochSecond(s.dueDate()).atZone(fuso));
        return "Olá, " + nome + "!\n\n"
                + "Notamos que a tarefa \"" + s.assignmentName() + "\" ainda não foi enviada "
                + "e o prazo termina em " + prazo + ".\n\n"
                + "Acesse a tarefa: " + s.link() + "\n\n"
                + "Se você já enviou, desconsidere esta mensagem.\n";
    }
}
