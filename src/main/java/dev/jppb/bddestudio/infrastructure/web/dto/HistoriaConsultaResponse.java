package dev.jppb.bddestudio.infrastructure.web.dto;

import dev.jppb.bddestudio.application.service.ConsultaProjetoService;
import dev.jppb.bddestudio.domain.model.HistoriaUsuario;
import dev.jppb.bddestudio.domain.model.Prioridade;

import java.time.LocalDateTime;

public record HistoriaConsultaResponse(
        Long id, Long projetoId, String titulo, String papel, String objetivo, String beneficio,
        Prioridade prioridade, Long autorId, String autorNome, LocalDateTime atualizadaEm,
        long quantidadeCenarios, long sugestoesPendentes) {

    public static HistoriaConsultaResponse from(ConsultaProjetoService.HistoriaComContagens c) {
        HistoriaUsuario h = c.historia();
        return new HistoriaConsultaResponse(h.getId(), h.getProjeto().getId(), h.getTitulo(),
                h.getPapel(), h.getObjetivo(), h.getBeneficio(), h.getPrioridade(),
                h.getAutor().getId(), h.getAutor().getNome(), h.getAtualizadaEm(),
                c.quantidadeCenarios(), c.sugestoesPendentes());
    }
}
