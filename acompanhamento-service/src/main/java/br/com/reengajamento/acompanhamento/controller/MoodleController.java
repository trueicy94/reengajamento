package br.com.reengajamento.acompanhamento.controller;

import br.com.reengajamento.acompanhamento.service.MoodleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

@RestController
@RequestMapping("/api/cursos")
public class MoodleController {

    private final MoodleService moodleService;

    public MoodleController(MoodleService moodleService) {
        this.moodleService = moodleService;
    }

    @GetMapping("/{courseId}/atividades")
    public JsonNode buscarAtividades(@PathVariable Long courseId) {
        return moodleService.buscarAtividades(courseId);
    }

    @GetMapping("/{courseId}/alunos")
    public JsonNode buscarAlunos(@PathVariable Long courseId) {
        return moodleService.buscarAlunos(courseId);
    }

    @GetMapping("/{assignmentId}/entregas")
    public JsonNode buscarEntregas(@PathVariable Long assignmentId) {
        return moodleService.buscarEntregas(assignmentId);
    }

    @GetMapping("/{courseId}/atividades/proximas-do-prazo")
    public JsonNode buscarAtividadesProximasDoPrazo(
            @PathVariable Long courseId) {
        return moodleService.buscarAtividadesProximasDoPrazo(courseId);
    }

    @GetMapping("/{courseId}/atividades/{assignmentId}/alunos-pendentes")
    public JsonNode buscarAlunosSemEntrega(
            @PathVariable Long courseId,
            @PathVariable Long assignmentId) {
        return moodleService.buscarAlunosSemEntrega(courseId, assignmentId);
    }

    @PostMapping("/{courseId}/processar-reengajamento")
    public String processarReengajamento(
            @PathVariable Long courseId) {
        moodleService.processarReengajamento(courseId);

        return "Processamento de reengajamento iniciado para o curso " + courseId;
    }
}