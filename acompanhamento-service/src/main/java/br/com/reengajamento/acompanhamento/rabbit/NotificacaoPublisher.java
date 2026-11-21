package br.com.reengajamento.acompanhamento.rabbit;

import br.com.reengajamento.acompanhamento.dto.SolicitacaoNotificacao;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NotificacaoPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${notificacoes.fila.principal}")
    private String filaPrincipal;

    public NotificacaoPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void enviarNotificacao(SolicitacaoNotificacao solicitacao) {
        rabbitTemplate.convertAndSend(filaPrincipal, solicitacao);
    }
}