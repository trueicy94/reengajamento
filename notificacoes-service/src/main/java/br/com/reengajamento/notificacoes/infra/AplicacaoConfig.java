package br.com.reengajamento.notificacoes.infra;

import br.com.reengajamento.notificacoes.aplicacao.EnviadorEmail;
import br.com.reengajamento.notificacoes.aplicacao.ProcessadorSolicitacao;
import br.com.reengajamento.notificacoes.aplicacao.RegistroNotificacoes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AplicacaoConfig {

    @Bean
    ProcessadorSolicitacao processadorSolicitacao(
            EnviadorEmail enviador,
            RegistroNotificacoes registro,
            @Value("${notificacoes.max-tentativas}") int maxTentativas) {
        return new ProcessadorSolicitacao(enviador, registro, maxTentativas);
    }
}
