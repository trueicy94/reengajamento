package br.com.reengajamento.notificacoes.infra;

import br.com.reengajamento.notificacoes.aplicacao.EnviadorEmail;
import br.com.reengajamento.notificacoes.dominio.MensagemLembrete;
import br.com.reengajamento.notificacoes.dominio.SolicitacaoReengajamento;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/** Envia o lembrete por SMTP (Mailpit nos testes). */
@Component
public class EnviadorEmailSmtp implements EnviadorEmail {

    private final JavaMailSender mailSender;
    private final String remetente;
    private final ZoneId fuso;

    public EnviadorEmailSmtp(
            JavaMailSender mailSender,
            @Value("${notificacoes.email.remetente}") String remetente,
            @Value("${notificacoes.timezone}") String fuso) {
        this.mailSender = mailSender;
        this.remetente = remetente;
        this.fuso = ZoneId.of(fuso);
    }

    @Override
    public void enviar(SolicitacaoReengajamento s) {
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(s.studentEmail());
        mensagem.setSubject(MensagemLembrete.assunto(s));
        mensagem.setText(MensagemLembrete.corpo(s, fuso));
        mailSender.send(mensagem);
    }
}
