package br.com.reengajamento.notificacoes.infra;

import br.com.reengajamento.notificacoes.aplicacao.ProcessadorSolicitacao;
import br.com.reengajamento.notificacoes.dominio.ResultadoProcessamento;
import br.com.reengajamento.notificacoes.dominio.SolicitacaoInvalidaException;
import br.com.reengajamento.notificacoes.dominio.SolicitacaoReengajamento;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Consome as solicitações do Acompanhamento. O resultado do processamento define o destino:
 * sucesso/duplicada = só confirma; nova tentativa = fila de espera; falha definitiva ou
 * mensagem inválida = fila de falhas. A mensagem é confirmada (ack) ao fim do método.
 */
@Component
public class SolicitacaoListener {

    private static final Logger log = LoggerFactory.getLogger(SolicitacaoListener.class);

    private final SolicitacaoParser parser;
    private final ProcessadorSolicitacao processador;
    private final RabbitTemplate rabbit;
    private final String filaRetry;
    private final String filaFalhas;

    public SolicitacaoListener(
            SolicitacaoParser parser,
            ProcessadorSolicitacao processador,
            RabbitTemplate rabbit,
            @Value("${notificacoes.fila.retry}") String filaRetry,
            @Value("${notificacoes.fila.falhas}") String filaFalhas) {
        this.parser = parser;
        this.processador = processador;
        this.rabbit = rabbit;
        this.filaRetry = filaRetry;
        this.filaFalhas = filaFalhas;
    }

    @RabbitListener(queues = "${notificacoes.fila.principal}")
    public void receber(Message mensagem) {
        byte[] corpo = mensagem.getBody();
        String json = new String(corpo, StandardCharsets.UTF_8);

        SolicitacaoReengajamento solicitacao;
        try {
            solicitacao = parser.parse(json);
        } catch (SolicitacaoInvalidaException e) {
            log.error("solicitação inválida ({}): {}", e.getMessage(), json);
            republicar(filaFalhas, corpo);
            return;
        }

        try {
            ResultadoProcessamento resultado = processador.processar(solicitacao);
            switch (resultado) {
                case ENVIADO -> log.info("lembrete enviado: {} -> {}",
                        solicitacao.requestId(), solicitacao.studentEmail());
                case DUPLICADA -> log.info("{} já enviada; ignorando duplicata", solicitacao.requestId());
                case NOVA_TENTATIVA -> {
                    log.warn("falha ao enviar {}; nova tentativa pela fila de espera", solicitacao.requestId());
                    republicar(filaRetry, corpo);
                }
                case FALHA_DEFINITIVA -> {
                    log.error("tentativas esgotadas para {}; movida para a fila de falhas", solicitacao.requestId());
                    republicar(filaFalhas, corpo);
                }
            }
        } catch (RuntimeException e) {
            // ex.: banco indisponível. Não repetir em loop: vai para a fila de falhas.
            log.error("erro inesperado ao processar {}", solicitacao.requestId(), e);
            republicar(filaFalhas, corpo);
        }
    }

    private void republicar(String fila, byte[] corpo) {
        Message nova = MessageBuilder.withBody(corpo)
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .build();
        rabbit.send("", fila, nova);
    }
}
