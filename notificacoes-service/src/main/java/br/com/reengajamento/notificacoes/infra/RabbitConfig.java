package br.com.reengajamento.notificacoes.infra;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topologia das filas (declaradas automaticamente ao conectar):
 * <ul>
 *   <li>principal: o Acompanhamento publica aqui (declare igual: durável, sem argumentos);</li>
 *   <li>retry: fila de espera; ao expirar o TTL a mensagem volta para a principal;</li>
 *   <li>falhas: tentativas esgotadas ou mensagens inválidas, para inspeção.</li>
 * </ul>
 */
@Configuration
public class RabbitConfig {

    @Bean
    Queue filaPrincipal(@Value("${notificacoes.fila.principal}") String nome) {
        return QueueBuilder.durable(nome).build();
    }

    @Bean
    Queue filaRetry(
            @Value("${notificacoes.fila.retry}") String nome,
            @Value("${notificacoes.fila.principal}") String principal,
            @Value("${notificacoes.retry.atraso-ms}") long atrasoMs) {
        return QueueBuilder.durable(nome)
                .ttl((int) atrasoMs)
                .deadLetterExchange("")
                .deadLetterRoutingKey(principal)
                .build();
    }

    @Bean
    Queue filaFalhas(@Value("${notificacoes.fila.falhas}") String nome) {
        return QueueBuilder.durable(nome).build();
    }
}
