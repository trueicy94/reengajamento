package br.com.reengajamento.notificacoes.controller;

import br.com.reengajamento.notificacoes.aplicacao.RegistroNotificacoes;
import br.com.reengajamento.notificacoes.dominio.Notificacao;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Consulta do registro de resultados (apoio à demonstração e aos testes). */
@RestController
@RequestMapping("/api/notificacoes")
public class NotificacaoController {

    private final RegistroNotificacoes registro;

    public NotificacaoController(RegistroNotificacoes registro) {
        this.registro = registro;
    }

    @GetMapping
    public List<Notificacao> listar() {
        return registro.listar();
    }
}
